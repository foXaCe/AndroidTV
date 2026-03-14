package org.jellyfin.androidtv.ui.base.state

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.jellyfin.sdk.api.client.exception.ApiClientException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class UiErrorMappingTests :
	FunSpec({

		test("IOException maps to Network error") {
			val ex = IOException("connection failed")
			val result = ex.toUiError()

			result.shouldBeInstanceOf<UiError.Network>()
			result.cause shouldBe ex
		}

		test("SocketTimeoutException maps to Timeout error") {
			val ex = SocketTimeoutException("timed out")
			val result = ex.toUiError()

			result.shouldBeInstanceOf<UiError.Timeout>()
			result.cause shouldBe ex
		}

		test("UnknownHostException maps to Network error") {
			val ex = UnknownHostException("unknown host")
			val result = ex.toUiError()

			result.shouldBeInstanceOf<UiError.Network>()
			result.cause shouldBe ex
		}

		test("ApiClientException with IOException cause maps to Network error") {
			val cause = IOException("network failure")
			val ex = ApiClientException("api error", cause)
			val result = ex.toUiError()

			result.shouldBeInstanceOf<UiError.Network>()
			result.cause shouldBe ex
		}

		test("ApiClientException with SocketTimeoutException cause maps to Timeout error") {
			val cause = SocketTimeoutException("timeout")
			val ex = ApiClientException("api error", cause)
			val result = ex.toUiError()

			result.shouldBeInstanceOf<UiError.Timeout>()
			result.cause shouldBe ex
		}

		test("ApiClientException with UnknownHostException cause maps to Network error") {
			val cause = UnknownHostException("unknown host")
			val ex = ApiClientException("api error", cause)
			val result = ex.toUiError()

			result.shouldBeInstanceOf<UiError.Network>()
			result.cause shouldBe ex
		}

		test("ApiClientException with generic cause maps to Server error") {
			val ex = ApiClientException("server error", IllegalStateException("bad state"))
			val result = ex.toUiError()

			result.shouldBeInstanceOf<UiError.Server>()
			result.cause shouldBe ex
		}

		test("ApiClientException with null cause maps to Server error") {
			val ex = ApiClientException("server error")
			val result = ex.toUiError()

			result.shouldBeInstanceOf<UiError.Server>()
			result.cause shouldBe ex
		}

		test("Unknown exception maps to Unknown error") {
			val ex = IllegalArgumentException("bad arg")
			val result = ex.toUiError()

			result.shouldBeInstanceOf<UiError.Unknown>()
			result.cause shouldBe ex
		}

		test("RuntimeException maps to Unknown error") {
			val ex = RuntimeException("unexpected")
			val result = ex.toUiError()

			result.shouldBeInstanceOf<UiError.Unknown>()
			result.cause shouldBe ex
		}
	})
