package com.example.restapisample

import com.example.restapisample.auth.JwtService
import com.example.restapisample.auth.MySession
import com.example.restapisample.auth.hash
import com.example.restapisample.repository.DatabaseFactory
import com.example.restapisample.repository.TodoRepository
import com.example.restapisample.routes.todos
import com.example.restapisample.routes.users
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.sessions.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.gson.*
import io.ktor.features.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Locations) {
    }

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    DatabaseFactory.init()
    val db = TodoRepository()
    val jwtService = JwtService()
    val hashFunction = { s: String -> hash(s) }

    install(Authentication) {
        jwt("jwt") {
            verifier(jwtService.verifier)
            realm = "Todo Server"
            validate {
                val payload = it.payload
                val claim = payload.getClaim("id")
                val claimString = claim.asInt()
                val user = db.findUser(claimString)
                user
            }
        }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        users(db,jwtService,hashFunction)
        todos(db)
    }
}

//@Location("/location/{name}")
//class MyLocation(val name: String, val arg1: Int = 42, val arg2: String = "default")

//@Location("/type/{name}")
//data class Type(val name: String) {
//    @Location("/edit")
//    data class Edit(val type: Type)
//
//    @Location("/list/{page}")
//    data class List(val type: Type, val page: Int)
//}

data class MySession(val count: Int = 0)

const val API_VERSION = "/v1"


