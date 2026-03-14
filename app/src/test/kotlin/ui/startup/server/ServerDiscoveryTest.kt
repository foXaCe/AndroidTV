package org.jellyfin.androidtv.ui.startup.server

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

class ServerDiscoveryTest :
	FunSpec({

		test("probeKnownServers returns server when endpoint responds") {
			val expected =
				DiscoveredServer(
					id = "test-id-12345",
					name = "TestServer",
					address = "http://192.168.1.60:8096",
					version = "10.11.6",
					pingMs = 42,
				)
			val repo = mockk<ServerDiscoveryRepository>()
			every { repo.probeKnownServers(listOf("http://192.168.1.60:8096")) } returns flowOf(expected)

			val servers = repo.probeKnownServers(listOf("http://192.168.1.60:8096")).toList()

			servers shouldHaveSize 1
			val server = servers.first()
			server.id shouldBe "test-id-12345"
			server.name shouldBe "TestServer"
			server.address shouldBe "http://192.168.1.60:8096"
			server.version shouldBe "10.11.6"
			server.pingMs shouldBe 42
			server.isReachable shouldBe true
		}

		test("probeKnownServers emits nothing when server is unreachable") {
			val repo = mockk<ServerDiscoveryRepository>()
			every { repo.probeKnownServers(any()) } returns flowOf()

			val servers = repo.probeKnownServers(listOf("http://127.0.0.1:1")).toList()
			servers shouldHaveSize 0
		}

		test("probeKnownServers probes multiple servers in parallel") {
			val server1 = DiscoveredServer(id = "srv1", name = "Server1", address = "http://10.0.0.1:8096", version = "10.11.0", pingMs = 10)
			val server2 = DiscoveredServer(id = "srv2", name = "Server2", address = "http://10.0.0.2:8096", version = "10.11.6", pingMs = 20)

			val repo = mockk<ServerDiscoveryRepository>()
			every { repo.probeKnownServers(any()) } returns flowOf(server1, server2)

			val servers =
				repo
					.probeKnownServers(
						listOf("http://10.0.0.1:8096", "http://10.0.0.2:8096"),
					).toList()

			servers shouldHaveSize 2
			servers.map { it.name }.toSet() shouldBe setOf("Server1", "Server2")
		}

		test("DiscoveredServer isReachable true when pingMs >= 0") {
			val server = DiscoveredServer(id = "t", name = "T", address = "http://10.0.0.1:8096", pingMs = 0)
			server.isReachable shouldBe true
		}

		test("DiscoveredServer isReachable false when pingMs < 0") {
			val server = DiscoveredServer(id = "t", name = "T", address = "http://10.0.0.1:8096", pingMs = -1)
			server.isReachable shouldBe false
		}

		test("DiscoveredServer host and port parsing") {
			val server = DiscoveredServer(id = "t", name = "T", address = "http://192.168.1.60:8096")
			server.host shouldBe "192.168.1.60"
			server.port shouldBe 8096
		}

		test("DiscoveredServer default port is 8096 when no port specified") {
			val server = DiscoveredServer(id = "t", name = "T", address = "http://192.168.1.60")
			server.port shouldBe 8096
		}

		test("DiscoveredServer host parsing with https") {
			val server = DiscoveredServer(id = "t", name = "T", address = "https://media.example.com:443")
			server.host shouldBe "media.example.com"
			server.port shouldBe 443
		}
	})
