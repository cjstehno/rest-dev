package restdev

data class KotlinUser(val id: Long?, val username: String, val email: String) {

    fun toJson(): String {
        return "{\"id\":$id, \"username\":\"$username\", \"email\":\"$email\"}"
    }

    companion object {
        fun fromJson(json: Map<String, Any>): KotlinUser {
            val id = when (json.get("id")) {
                null -> null
                else -> json.get("id").toString().toLong()
            }
            return KotlinUser(id, json.get("username") as String, json.get("email") as String)
        }
    }
}

