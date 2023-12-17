CREATE TABLE search_history (
    user_login text,
    origin text,
    departure_date text,
    one_way boolean not null,
    max_price int,
    travel_duration_lower int,
    travel_duration_upper int
)
