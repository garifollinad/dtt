package com.example.ddtapp.repository

import com.example.ddtapp.database.Database
import com.example.ddtapp.model.House
import io.reactivex.Flowable
import io.reactivex.Single

interface HouseDaoRepository {
    fun getHouses(): Single<List<House>>
    fun insertHouses(houses: List<House>)

    fun getHouseById(id: String): Single<House?>

    fun getHousesZipAndCityFiltered(zip: String, city: String): Flowable<List<House>>

    fun getHousesZipOrCityFiltered(filter: String): Flowable<List<House>>
}

class HouseDaoRepositoryImpl(private val database: Database) :
    HouseDaoRepository {
    override fun getHouses(): Single<List<House>> {
        return database.houseDao().getHouses()
    }

    override fun insertHouses(houses: List<House>) {
        database.houseDao().insertHouses(houses)
    }

    override fun getHouseById(id: String): Single<House?> {
        return database.houseDao().getHouseById(id)
    }

    override fun getHousesZipAndCityFiltered(zip: String, city: String): Flowable<List<House>> {
        return database.houseDao().getHousesZipAndCityFiltered(zip, city)
    }

    override fun getHousesZipOrCityFiltered(filter: String): Flowable<List<House>> {
        return database.houseDao().getHousesZipOrCityFiltered(filter)
    }
}