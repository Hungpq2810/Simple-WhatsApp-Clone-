package com.example.messengerapp.model

class ChatList {
    private var id: String = ""

    constructor()
    constructor(id: String) {
        this.id = id
    }

    fun getId(): String {
        return id
    }

    fun setId(id: String) {
        this.id = id
    }
}