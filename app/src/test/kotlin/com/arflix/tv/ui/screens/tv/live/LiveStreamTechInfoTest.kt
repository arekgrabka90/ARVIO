package com.arflix.tv.ui.screens.tv.live

import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LiveStreamTechInfoTest {
    @Test fun missingFormatsClearAllFields() {
        assertThat(liveStreamTechInfo(null, null)).isEqualTo(LiveStreamTechInfo())
    }

    @Test fun reportsKnownFormatsAndCombinedAverageBitrate() {
        val video = Format.Builder().setHeight(1080).setFrameRate(50f)
            .setSampleMimeType(MimeTypes.VIDEO_H264).setAverageBitrate(5_000_000).build()
        val audio = Format.Builder().setChannelCount(6).setSampleMimeType(MimeTypes.AUDIO_E_AC3)
            .setAverageBitrate(200_000).build()
        assertThat(liveStreamTechInfo(video, audio)).isEqualTo(
            LiveStreamTechInfo("1080p", "50 fps", "h264", "5.1 eac3", "~5.2 Mbps", "FHD", "5.1"))
    }

    @Test fun unknownNewStreamCannotRetainPreviousValues() {
        val video = Format.Builder().setHeight(2160).setFrameRate(60f).build()
        assertThat(liveStreamTechInfo(video, null).fps).isEqualTo("60 fps")
        assertThat(liveStreamTechInfo(video, null).qualityLabel).isEqualTo("4K")
        assertThat(liveStreamTechInfo(Format.Builder().build(), null)).isEqualTo(LiveStreamTechInfo())
    }

    @Test fun preservesFractionalFpsAndCorrectAv1Mime() {
        val video = Format.Builder().setFrameRate(23.976f).setSampleMimeType(MimeTypes.VIDEO_AV1).build()
        val info = liveStreamTechInfo(video, null)
        assertThat(info.fps).isEqualTo("23.98 fps")
        assertThat(info.videoCodec).isEqualTo("av1")
    }

    @Test fun mapsResolutionAndAudioChannelLabels() {
        val cases = listOf(
            2160 to "4K",
            1080 to "FHD",
            720 to "HD",
            576 to "SD",
        )
        cases.forEach { (height, expected) ->
            assertThat(liveStreamTechInfo(Format.Builder().setHeight(height).build(), null).qualityLabel)
                .isEqualTo(expected)
        }
        assertThat(liveStreamTechInfo(null, Format.Builder().setChannelCount(1).build()).audioLabel).isEqualTo("MONO")
        assertThat(liveStreamTechInfo(null, Format.Builder().setChannelCount(2).build()).audioLabel).isEqualTo("STEREO")
        assertThat(liveStreamTechInfo(null, Format.Builder().setChannelCount(6).build()).audioLabel).isEqualTo("5.1")
        assertThat(liveStreamTechInfo(null, Format.Builder().setChannelCount(8).build()).audioLabel).isEqualTo("7.1")
        assertThat(liveStreamTechInfo(null, Format.Builder().build()).audioLabel).isEmpty()
    }

    @Test fun missingAudioBitrateDoesNotPretendVideoBitrateIsTotal() {
        val video = Format.Builder().setAverageBitrate(5_000_000).build()
        assertThat(liveStreamTechInfo(video, Format.Builder().build()).bitrate).isEmpty()
    }

    @Test fun invalidFpsIsHidden() {
        for (fps in listOf(-1f, 0f, Float.NaN, Float.POSITIVE_INFINITY)) {
            assertThat(liveStreamTechInfo(Format.Builder().setFrameRate(fps).build(), null).fps).isEmpty()
        }
    }
}
