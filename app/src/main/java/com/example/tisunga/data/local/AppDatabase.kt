package com.example.tisunga.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tisunga.data.local.dao.GroupDao
import com.example.tisunga.data.local.dao.UserDao
import com.example.tisunga.data.local.entity.GroupEntity
import com.example.tisunga.data.local.entity.UserEntity

@Database(entities = [UserEntity::class, GroupEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
}
