import di.appModule
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.netty.*
import org.koin.ktor.ext.Koin
import routes.apiRoutes
import routes.bannerRoutes
import routes.siteRoutes
import routes.staticRoutes

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    install(Koin) {
        modules(appModule)
    }
    install(XForwardedHeaderSupport)
    install(ContentNegotiation) {
        json()
    }
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    routing {
        bannerRoutes()
        apiRoutes()
        siteRoutes()
        staticRoutes()
    }
}
