package com.flyingantsstudios.esmtest;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ContactList.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ContactListDao contactListDao();

}