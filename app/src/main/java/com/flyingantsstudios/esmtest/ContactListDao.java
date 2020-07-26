package com.flyingantsstudios.esmtest;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ContactListDao {

    @Query("SELECT * FROM ContactList")
    List<ContactList> getAll();

    @Query("SELECT * FROM ContactList WHERE uid1 LIKE :first")
    List<ContactList> contact1(String first);

    @Query("SELECT * FROM ContactList WHERE uid2 LIKE :first")
    List<ContactList> contact2(String first);

    @Query("SELECT uid2 FROM ContactList WHERE displayname LIKE :first")
    String getUserId(String first);

    @Query("UPDATE ContactList SET status=2 WHERE displayname LIKE :first")
    void updateStatus(String first);

    @Insert
    void insert(ContactList contactLists);

    @Delete
    void delete(ContactList contactList);
}