import di.appModule
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import org.koin.ktor.ext.Koin
import routes.bannerRoutes

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    install(Koin) {
        modules(appModule)
    }

    routing {
        bannerRoutes()
    }
}
