-- DROP SCHEMA public;

--CREATE SCHEMA public AUTHORIZATION postgres;

-- DROP TYPE mpaa_rating;

CREATE TYPE mpaa_rating AS ENUM (
  'G',
  'PG',
  'PG-13',
  'R',
  'NC-17');


CREATE SEQUENCE public.actor_actor_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;
-- DROP SEQUENCE public.address_address_id_seq;

CREATE SEQUENCE public.address_address_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;
-- DROP SEQUENCE public.category_category_id_seq;

CREATE SEQUENCE public.category_category_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;
-- DROP SEQUENCE public.city_city_id_seq;

CREATE SEQUENCE public.city_city_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;
-- DROP SEQUENCE public.country_country_id_seq;

CREATE SEQUENCE public.country_country_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;
-- DROP SEQUENCE public.customer_customer_id_seq;

CREATE SEQUENCE public.customer_customer_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;
-- DROP SEQUENCE public.film_film_id_seq;

CREATE SEQUENCE public.film_film_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;
-- DROP SEQUENCE public.inventory_inventory_id_seq;

CREATE SEQUENCE public.inventory_inventory_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;
-- DROP SEQUENCE public.language_language_id_seq;

CREATE SEQUENCE public.language_language_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;
-- DROP SEQUENCE public.payment_payment_id_seq;

CREATE SEQUENCE public.payment_payment_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;
-- DROP SEQUENCE public.rental_rental_id_seq;

CREATE SEQUENCE public.rental_rental_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;
-- DROP SEQUENCE public.staff_staff_id_seq;

CREATE SEQUENCE public.staff_staff_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;
-- DROP SEQUENCE public.store_store_id_seq;

CREATE SEQUENCE public.store_store_id_seq
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1
  NO CYCLE;-- public.actor definition

-- Drop table

-- DROP TABLE actor;

CREATE TABLE actor (
  actor_id serial NOT NULL,
  first_name varchar(45) NOT NULL,
  last_name varchar(45) NOT NULL,
  last_update timestamp NOT NULL DEFAULT now(),
  CONSTRAINT actor_pkey PRIMARY KEY (actor_id)
);
CREATE INDEX idx_actor_last_name ON public.actor USING btree (last_name);

-- public.category definition

-- Drop table

-- DROP TABLE category;

CREATE TABLE category (
  category_id serial NOT NULL,
  "name" varchar(25) NOT NULL,
  last_update timestamp NOT NULL DEFAULT now(),
  CONSTRAINT category_pkey PRIMARY KEY (category_id)
);

-- Table Triggers

-- public.country definition

-- Drop table

-- DROP TABLE country;

CREATE TABLE country (
  country_id serial NOT NULL,
  country varchar(50) NOT NULL,
  last_update timestamp NOT NULL DEFAULT now(),
  CONSTRAINT country_pkey PRIMARY KEY (country_id)
);

-- Table Triggers

-- public."language" definition

-- Drop table

-- DROP TABLE "language";

CREATE TABLE "language" (
  language_id serial NOT NULL,
  "name" bpchar(20) NOT NULL,
  last_update timestamp NOT NULL DEFAULT now(),
  CONSTRAINT language_pkey PRIMARY KEY (language_id)
);


-- public.city definition

-- Drop table

-- DROP TABLE city;

CREATE TABLE city (
  city_id serial NOT NULL,
  city varchar(50) NOT NULL,
  country_id int2 NOT NULL,
  last_update timestamp NOT NULL DEFAULT now(),
  CONSTRAINT city_pkey PRIMARY KEY (city_id),
  CONSTRAINT fk_city FOREIGN KEY (country_id) REFERENCES country(country_id)
);
CREATE INDEX idx_fk_country_id ON public.city USING btree (country_id);

-- Table Triggers


-- public.film definition

-- Drop table

-- DROP TABLE film;

CREATE TABLE film (
  film_id serial NOT NULL,
  title varchar(255) NOT NULL,
  description text NULL,
  release_year integer NULL,
  language_id int2 NOT NULL,
  rental_duration int2 NOT NULL DEFAULT 3,
  rental_rate numeric(4,2) NOT NULL DEFAULT 4.99,
  length int2 NULL,
  replacement_cost numeric(5,2) NOT NULL DEFAULT 19.99,
  rating mpaa_rating NULL DEFAULT 'G'::mpaa_rating,
  last_update timestamp NOT NULL DEFAULT now(),
  special_features _text NULL,
  fulltext tsvector NOT NULL,
  CONSTRAINT film_pkey PRIMARY KEY (film_id),
  CONSTRAINT film_language_id_fkey FOREIGN KEY (language_id) REFERENCES language(language_id) ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX film_fulltext_idx ON public.film USING gist (fulltext);
CREATE INDEX idx_fk_language_id ON public.film USING btree (language_id);
CREATE INDEX idx_title ON public.film USING btree (title);

-- Table Triggers


-- public.film_actor definition

-- Drop table

-- DROP TABLE film_actor;

CREATE TABLE film_actor (
  actor_id int2 NOT NULL,
  film_id int2 NOT NULL,
  last_update timestamp NOT NULL DEFAULT now(),
  CONSTRAINT film_actor_pkey PRIMARY KEY (actor_id, film_id),
  CONSTRAINT film_actor_actor_id_fkey FOREIGN KEY (actor_id) REFERENCES actor(actor_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT film_actor_film_id_fkey FOREIGN KEY (film_id) REFERENCES film(film_id) ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX idx_fk_film_id ON public.film_actor USING btree (film_id);


-- public.film_category definition

-- Drop table

-- DROP TABLE film_category;

CREATE TABLE film_category (
  film_id int2 NOT NULL,
  category_id int2 NOT NULL,
  last_update timestamp NOT NULL DEFAULT now(),
  CONSTRAINT film_category_pkey PRIMARY KEY (film_id, category_id),
  CONSTRAINT film_category_category_id_fkey FOREIGN KEY (category_id) REFERENCES category(category_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT film_category_film_id_fkey FOREIGN KEY (film_id) REFERENCES film(film_id) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- public.inventory definition

-- Drop table

-- DROP TABLE inventory;

CREATE TABLE inventory (
  inventory_id serial NOT NULL,
  film_id int2 NOT NULL,
  store_id int2 NOT NULL,
  last_update timestamp NOT NULL DEFAULT now(),
  CONSTRAINT inventory_pkey PRIMARY KEY (inventory_id),
  CONSTRAINT inventory_film_id_fkey FOREIGN KEY (film_id) REFERENCES film(film_id) ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX idx_store_id_film_id ON public.inventory USING btree (store_id, film_id);

-- Table Triggers

-- public.address definition

-- Drop table

-- DROP TABLE address;

CREATE TABLE address (
  address_id serial NOT NULL,
  address varchar(50) NOT NULL,
  address2 varchar(50) NULL,
  district varchar(20) NOT NULL,
  city_id int2 NOT NULL,
  postal_code varchar(10) NULL,
  phone varchar(20) NOT NULL,
  last_update timestamp NOT NULL DEFAULT now(),
  CONSTRAINT address_pkey PRIMARY KEY (address_id),
  CONSTRAINT fk_address_city FOREIGN KEY (city_id) REFERENCES city(city_id)
);
CREATE INDEX idx_fk_city_id ON public.address USING btree (city_id);

-- Table Triggers


-- public.customer definition

-- Drop table

-- DROP TABLE customer;

CREATE TABLE customer (
  customer_id serial NOT NULL,
  store_id int2 NOT NULL,
  first_name varchar(45) NOT NULL,
  last_name varchar(45) NOT NULL,
  email varchar(50) NULL,
  address_id int2 NOT NULL,
  activebool bool NOT NULL DEFAULT true,
  create_date date NOT NULL DEFAULT 'now'::text::date,
  last_update timestamp NULL DEFAULT now(),
  active int4 NULL,
  CONSTRAINT customer_pkey PRIMARY KEY (customer_id),
  CONSTRAINT customer_address_id_fkey FOREIGN KEY (address_id) REFERENCES address(address_id) ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX idx_fk_address_id ON public.customer USING btree (address_id);
CREATE INDEX idx_fk_store_id ON public.customer USING btree (store_id);
CREATE INDEX idx_last_name ON public.customer USING btree (last_name);

-- Table Triggers


-- public.staff definition

-- Drop table

-- DROP TABLE staff;

CREATE TABLE staff (
  staff_id serial NOT NULL,
  first_name varchar(45) NOT NULL,
  last_name varchar(45) NOT NULL,
  address_id int2 NOT NULL,
  email varchar(50) NULL,
  store_id int2 NOT NULL,
  active bool NOT NULL DEFAULT true,
  username varchar(16) NOT NULL,
  password varchar(40) NULL,
  last_update timestamp NOT NULL DEFAULT now(),
  picture bytea NULL,
  CONSTRAINT staff_pkey PRIMARY KEY (staff_id),
  CONSTRAINT staff_address_id_fkey FOREIGN KEY (address_id) REFERENCES address(address_id) ON UPDATE CASCADE ON DELETE RESTRICT
);


-- public."store" definition

-- Drop table

-- DROP TABLE "store";

CREATE TABLE "store" (
  store_id serial NOT NULL,
  manager_staff_id int2 NOT NULL,
  address_id int2 NOT NULL,
  last_update timestamp NOT NULL DEFAULT now(),
  CONSTRAINT store_pkey PRIMARY KEY (store_id),
  CONSTRAINT store_address_id_fkey FOREIGN KEY (address_id) REFERENCES address(address_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT store_manager_staff_id_fkey FOREIGN KEY (manager_staff_id) REFERENCES staff(staff_id) ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE UNIQUE INDEX idx_unq_manager_staff_id ON public.store USING btree (manager_staff_id);


-- public.rental definition

-- Drop table

-- DROP TABLE rental;

CREATE TABLE rental (
  rental_id serial NOT NULL,
  rental_date timestamp NOT NULL,
  inventory_id int4 NOT NULL,
  customer_id int2 NOT NULL,
  return_date timestamp NULL,
  staff_id int2 NOT NULL,
  last_update timestamp NOT NULL DEFAULT now(),
  CONSTRAINT rental_pkey PRIMARY KEY (rental_id),
  CONSTRAINT rental_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT rental_inventory_id_fkey FOREIGN KEY (inventory_id) REFERENCES inventory(inventory_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT rental_staff_id_key FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
);
CREATE INDEX idx_fk_inventory_id ON public.rental USING btree (inventory_id);
CREATE UNIQUE INDEX idx_unq_rental_rental_date_inventory_id_customer_id ON public.rental USING btree (rental_date, inventory_id, customer_id);

-- Table Triggers

-- public.payment definition

-- Drop table

-- DROP TABLE payment;

CREATE TABLE payment (
  payment_id serial NOT NULL,
  customer_id int2 NOT NULL,
  staff_id int2 NOT NULL,
  rental_id int4 NOT NULL,
  amount numeric(5,2) NOT NULL,
  payment_date timestamp NOT NULL,
  CONSTRAINT payment_pkey PRIMARY KEY (payment_id),
  CONSTRAINT payment_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT payment_rental_id_fkey FOREIGN KEY (rental_id) REFERENCES rental(rental_id) ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT payment_staff_id_fkey FOREIGN KEY (staff_id) REFERENCES staff(staff_id) ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX idx_fk_customer_id ON public.payment USING btree (customer_id);
CREATE INDEX idx_fk_rental_id ON public.payment USING btree (rental_id);
CREATE INDEX idx_fk_staff_id ON public.payment USING btree (staff_id);

