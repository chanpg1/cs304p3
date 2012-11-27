drop table BorrowerType;
 
create table BorrowerType
	(type varchar(8) not null PRIMARY KEY,	
	bookTimeLimit int not null );
 
grant select on BorrowerType to public;

drop table Borrower;

create table Borrower
	(bid number(10) not null PRIMARY KEY,
	password varchar(20) not null,
	name varchar(40) not null,
	address varchar(50),
	phone number(10),
	emailAddress varchar(30),
	sinOrStNo number(9) not null UNIQUE,
	expiryDate date not null,
	type varchar(8) not null,
	foreign key (type) references BorrowerType);
 
grant select on Borrower to public;
 
drop table Book;
 
create table Book
	(callNumber varchar(20) not null PRIMARY KEY,
	isbn varchar(13) not null UNIQUE,
	title varchar(40) not null,
	mainAuthor varchar(40) not null,
	publisher varchar(40) not null,
	year number(4) not null);
 
grant select on Book to public;
 
drop table HasAuthor;
 
create table HasAuthor
	(callNumber varchar(20) not null,
	name varchar(40) not null,
	PRIMARY KEY (callNumber, name),
	foreign key (callNumber) references Book);
 
grant select on HasAuthor to public;
 
drop table HasSubject;
 
create table HasSubject
	(callNumber varchar(20) not null,
	subject varchar(20) not null,
	PRIMARY KEY (callNumber, subject),
	foreign key (callNumber) references Book);
 
grant select on HasSubject to public;

drop table BookCopy;
 
create table BookCopy
	(callNumber varchar(20) not null,
	copyNo number(3) not null,
	status varchar(7) not null,
	PRIMARY KEY (callNumber, copyNo),
	foreign key (callNumber) references Book);
 
grant select on BookCopy to public;
 
drop table HoldRequest;
 
create table HoldRequest
	(hid number(10) not null PRIMARY KEY,
	bid number(10) not null,
	callNumber varchar(20) not null,
	issuedDate date not null,
	foreign key (bid) references Borrower,
	foreign key (callNumber) references Book);
 
grant select on HoldRequest to public;
 
drop table Borrowing;
 
create table Borrowing
	(borid number(15) not null PRIMARY KEY,
	bid number(10) not null,
	callNumber varchar(20) not null,
	copyNo number(3) not null,
	outDate date not null,
	inDate date,
	foreign key (bid) references Borrower,
	foreign key (callNumber, copyNo) references BookCopy(callNumber, copyNo));
 
grant select on Borrowing to public;
 
drop table Fine;
 
create table Fine
	(fid number(10) not null PRIMARY KEY,
	amount float not null,
	issueDate date not null,
	paidDate date ,
	borid number(15) not null,
	foreign key (borid) references Borrowing);
 
grant select on Fine to public;
 