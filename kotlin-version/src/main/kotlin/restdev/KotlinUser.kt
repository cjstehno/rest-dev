package restdev

data class KotlinUser(val id: Long?, val username: String, val email: String) {

    fun toJson(): String {
        return "{\"id\":$id, \"username\":\"$username\", \"email\":\"$email\"}"
    }

    companion object {
        fun fromJson(json: Map<String, Any>): KotlinUser {
            return KotlinUser(
                when (json.get("id")) {
                    null -> null
                    else -> json.get("id").toString().toLong()
                },
                json.get("username") as String,
                json.get("email") as String
            )
        }
    }
}

