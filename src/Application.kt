package com.kosten.ktonExample

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.features.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson {
        }
    }
    user()
}

/*
    User Model
 */
data class User(val id: Long, val name: String, val surname: String) {
    override fun equals(other: Any?): Boolean {
        if (other is User) {
            return other.id == id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int = id.hashCode()
}

/*
    Error Model
 */
data class Error(val message: String)

/*
    User Repository
 */
val users = mutableListOf<User>()


/*
    Routing
 */
// Operaciones CRUD sobre un modelo:
// get: Obtener todos los usuarios
// post: Crear un usuario nuevo
// put: para editar/actulizar un usuario
// delete: para borrar un usuario
fun Application.user() {
    routing {

        route("/user") {

            get { call.respond(users) }

            get("/{id}") {
                val candidateId = call.parameters["id"]?.toLongOrNull()
                val result =  when(candidateId) {
                    null -> call.respond(HttpStatusCode.BadRequest, Error("ID must be long"))
                    else -> {
                        val user = users.firstOrNull{ it.id == candidateId }
                        when(user) {
                            null -> call.respond(HttpStatusCode.NotFound, Error("User with id $candidateId not found"))
                            else -> call.respond(user)
                        }
                    }
                }
            }

            post {
                val candidate = call.receive<User>()
                val user = users.firstOrNull{ it.id == candidate.id }
                if (user != null) {
                    call.respond(HttpStatusCode.BadRequest, Error("User with id ${candidate.id} already exits"))
                } else {
                    users.add(candidate)
                    call.respond(HttpStatusCode.Created, "User Added")
                }
            }

            put {
                val candidate = call.receive<User>()
                val result = when(candidate) {
                    is User -> when(users.contains(candidate)) {
                        true -> {
                            users[users.indexOf(candidate)] = candidate
                            call.respond(HttpStatusCode.OK, "User Updated")
                        }
                        false -> call.respond(HttpStatusCode.NotFound, Error("User with id ${candidate.id} not found"))
                    }
                    else -> call.respond(HttpStatusCode.BadRequest, Error("This method only accepts User instances"))
                }
            }

            delete("/{id}") {
                val candidateId = call.parameters["id"]?.toLongOrNull()
                val result =  when(candidateId) {
                    null -> call.respond(HttpStatusCode.BadRequest, Error("ID must be long"))
                    else -> {
                        val user = users.firstOrNull{ it.id == candidateId }
                        when(user) {
                            null -> call.respond(HttpStatusCode.NotFound, Error("User with id $candidateId not found"))
                            else -> {
                                users.remove(user)
                                call.respond(HttpStatusCode.OK, "User $candidateId deleted")
                            }
                        }
                    }
                }
            }
        }
    }
}
