package com.flyingantsstudios.esmtest;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ContactList {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name="Uid1")
    public String uid1;
    @ColumnInfo(name="Uid2")
    public String uid2;
    @ColumnInfo(name="Status")
    public int status;
    @ColumnInfo(name="Displayname")
    public String displayname;
}
