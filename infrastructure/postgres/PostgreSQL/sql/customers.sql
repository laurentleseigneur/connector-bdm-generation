drop table if exists customers;


CREATE TABLE customers (
	customer_id varchar(250) null,
	car_manufacturer varchar(250) null,
	car_model varchar(250) null,
	car_color varchar(250) null,
	car_type varchar(250) null,
	car_gear varchar(250) null,
	coverage_product varchar(250) null,
	coverage_start_date date null,
	coverage_end_date date null,
	odometer integer null,
	km_covered integer null,
	car_owner varchar (250) null
);

insert into customers(customer_id,car_manufacturer,car_model,car_color,car_type, car_gear,coverage_product,coverage_start_date,coverage_end_date,odometer,km_covered,car_owner) values
('walter.bates','Renault','Clio','Red','Standart','Manual','Standart','2020-06-01','2021-06-01',153600,160000,'Bates, Walter'),
('helen.kelly','Audi','A3','Green','Break','Auto','Full','2020-09-10','2021-09-10',65000,80000,'Kelly,Helen');

select * from customers;