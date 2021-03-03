package com.example.countries;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface CountryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<Country> countries);

    @Query("SELECT * FROM country")
    Single<List<Country>> getAll();

    @Query("DELETE FROM country")
    Completable removeAll();

}
