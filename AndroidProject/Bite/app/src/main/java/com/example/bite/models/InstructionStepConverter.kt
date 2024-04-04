package com.example.bite.models

import androidx.room.TypeConverter
import com.example.bite.models.InstructionStep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class InstructionStepConverter {
    @TypeConverter
    fun fromInstructionStepList(value: List<InstructionStep>?): String? {
        if (value == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<InstructionStep>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toInstructionStepList(value: String?): List<InstructionStep>? {
        if (value == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<InstructionStep>>() {}.type
        return gson.fromJson(value, type)
    }
}