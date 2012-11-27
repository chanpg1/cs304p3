alter session set nls_date_format='DD/MM/YYYY';

INSERT INTO BorrowerType VALUES ('student', 14);
INSERT INTO BorrowerType VALUES ('faculty', 84);
INSERT INTO BorrowerType VALUES ('staff', 42);

INSERT INTO Borrower VALUES (1234, 'a62494042', 'Jackie Chan', '123 ABC St.', 6042573830, 'JC@email.com', 62494042, '08/08/2013', 'student');
INSERT INTO Borrower VALUES (5678, 'a2886', 'Andy Wen', 'Homeless' , '' , 'krazi_hobo123@wen.com', 123456222, '08/08/2053', 'faculty');
INSERT INTO Borrower VALUES (1337, 'ALVIN4EVER', 'Alvin Shen', '2442 Walnut Grove' , 24322021 , 'hot@gmail.com', 00756222, '08/09/2043', 'staff');
INSERT INTO Borrower VALUES (1338, 'so1337', 'Steven Pui', '2043 Smithe St.', 173173173, 'absolutesteven@email.com', 12345621, '12/12/2019', 'student');

INSERT INTO Book VALUES ('ALVINSHEN1', '123456789', 'Alvin the Cheap Monk', 'Alvin Shen', 'UBC CS304', 2012);
INSERT INTO Book VALUES ('STEVENPUI1', '987654321', 'Absolute Steven: Misread Emails', 'Steven Pui', 'UBC CS304', 1997);
INSERT INTO Book VALUES ('ANDYWEN1', '13371337', 'Wen is Andy Rong?', 'Andy Rong Wen', 'DOTA Press', 2012);
INSERT INTO Book VALUES ('JACKIECHAN1', '13381338', 'Jackie Chan Does Script-fu', 'Jackie Chan', 'JC Press', 2002);
INSERT INTO Book VALUES ('JACKIECHAN2', '13391339', 'Jackie Chan Methventures', 'Jackie Chan', 'JC Press', 1995);

INSERT INTO HasAuthor VALUES ('ALVINSHEN1', 'Alvin Shen');
INSERT INTO HasAuthor VALUES ('ALVINSHEN1', 'Shaolin Monks');
INSERT INTO HasAuthor VALUES ('STEVENPUI1', 'Steven Pui');
INSERT INTO HasAuthor VALUES ('STEVENPUI1', 'Steve Jobs');
INSERT INTO HasAuthor VALUES ('STEVENPUI1', 'Bill Gates');
INSERT INTO HasAuthor VALUES ('ANDYWEN1', 'Andy Rong Wen');
INSERT INTO HasAuthor VALUES ('ANDYWEN1', 'Andy Wen');
INSERT INTO HasAuthor VALUES ('ANDYWEN1', 'Rong Wen');
INSERT INTO HasAuthor VALUES ('JACKIECHAN1', 'Jackie Chan');
INSERT INTO HasAuthor VALUES ('JACKIECHAN2', 'Jackie Chan');
INSERT INTO HasAuthor VALUES ('JACKIECHAN2', 'Bruce Lee');
INSERT INTO HasAuthor VALUES ('JACKIECHAN2', 'Jet Li');
INSERT INTO HasAuthor VALUES ('JACKIECHAN2', 'Chuck Norris');
INSERT INTO HasAuthor VALUES ('JACKIECHAN2', 'Steven Segal');

INSERT INTO HasSubject VALUES ('ALVINSHEN1', 'Cheap Monks');
INSERT INTO HasSubject VALUES ('ALVINSHEN1', 'Shaolin');
INSERT INTO HasSubject VALUES ('ALVINSHEN1', 'Economics');
INSERT INTO HasSubject VALUES ('STEVENPUI1', 'Email');
INSERT INTO HasSubject VALUES ('STEVENPUI1', 'Technology');
INSERT INTO HasSubject VALUES ('STEVENPUI1', 'Language Processing');
INSERT INTO HasSubject VALUES ('ANDYWEN1', 'DOTA');
INSERT INTO HasSubject VALUES ('ANDYWEN1', 'Reddit');
INSERT INTO HasSubject VALUES ('ANDYWEN1', 'Misnomers');
INSERT INTO HasSubject VALUES ('JACKIECHAN1', 'Jackie Chan');
INSERT INTO HasSubject VALUES ('JACKIECHAN2', 'Martial Arts');
INSERT INTO HasSubject VALUES ('JACKIECHAN2', 'Adventure');
INSERT INTO HasSubject VALUES ('JACKIECHAN2', 'Substance Abuse');

INSERT INTO BookCopy VALUES ('ALVINSHEN1', 1, 'on-hold');
INSERT INTO BookCopy VALUES ('ALVINSHEN1', 2, 'in');
INSERT INTO BookCopy VALUES ('ALVINSHEN1', 3, 'out');
INSERT INTO BookCopy VALUES ('ANDYWEN1', 1, 'out');
INSERT INTO BookCopy VALUES ('STEVENPUI1', 1, 'in');
INSERT INTO BookCopy VALUES ('JACKIECHAN1', 1, 'in');
INSERT INTO BookCopy VALUES ('JACKIECHAN2', 1, 'in');

INSERT INTO HoldRequest VALUES (24, 1337, 'ANDYWEN1', '15/11/2012');

INSERT INTO Borrowing VALUES (56, 1337, 'STEVENPUI1', 1, '14/10/2011', '01/10/2012');
INSERT INTO Borrowing VALUES (57, 1234, 'STEVENPUI1', 1, '14/10/2012', '28/10/2012');
INSERT INTO Borrowing VALUES (58, 1338, 'ALVINSHEN1', 3, '14/11/2012', '');
INSERT INTO Borrowing VALUES (59, 1338, 'ANDYWEN1', 1, '14/11/2012', '');

INSERT INTO Fine VALUES (32, 50, '14/10/2012', '28/10/2012', 56);

COMMIT;







