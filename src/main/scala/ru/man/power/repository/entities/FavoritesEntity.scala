package ru.man.power.repository.entities

case class FavoritesEntity(
    user_login: String,
    origin: String,
    destination: String,
    departure_date: String,
    return_date: String,
    flightDates: String,
    flightOffers: String,
)
