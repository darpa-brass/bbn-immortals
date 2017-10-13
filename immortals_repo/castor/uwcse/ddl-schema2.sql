CREATE TABLE professorStudentCoauthor_train_pos (
   person1 VARCHAR(32),
   person2 VARCHAR(32)
);

CREATE TABLE professorStudentCoauthor_train_neg (
   person1 VARCHAR(32),
   person2 VARCHAR(32)
);

CREATE TABLE professorStudentCoauthor_test_pos (
   person1 VARCHAR(32),
   person2 VARCHAR(32)
);

CREATE TABLE professorStudentCoauthor_test_neg (
   person1 VARCHAR(32),
   person2 VARCHAR(32)
);

CREATE TABLE professorAdvancedStudentCoauthor_train_pos (
   person1 VARCHAR(32),
   person2 VARCHAR(32)
);

CREATE TABLE professorAdvancedStudentCoauthor_train_neg (
   person1 VARCHAR(32),
   person2 VARCHAR(32)
);

CREATE TABLE professorAdvancedStudentCoauthor_test_pos (
   person1 VARCHAR(32),
   person2 VARCHAR(32)
);

CREATE TABLE professorAdvancedStudentCoauthor_test_neg (
   person1 VARCHAR(32),
   person2 VARCHAR(32)
);

CREATE TABLE advisedby_train_pos (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_train_pos PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_train_neg (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_train_neg PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_test_pos (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_test_pos PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_test_neg (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_test_neg PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold1_train_pos (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold1_train_pos PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold1_train_neg (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold1_train_neg PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold1_test_pos (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold1_test_pos PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold1_test_neg (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold1_test_neg PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold2_train_pos (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold2_train_pos PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold2_train_neg (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold2_train_neg PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold2_test_pos (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold2_test_pos PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold2_test_neg (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold2_test_neg PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold3_train_pos (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold3_train_pos PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold3_train_neg (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold3_train_neg PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold3_test_pos (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold3_test_pos PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold3_test_neg (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold3_test_neg PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold4_train_pos (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold4_train_pos PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold4_train_neg (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold4_train_neg PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold4_test_pos (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold4_test_pos PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold4_test_neg (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold4_test_neg PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold5_train_pos (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold5_train_pos PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold5_train_neg (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold5_train_neg PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold5_test_pos (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold5_test_pos PRIMARY KEY (student,advisor)
);

CREATE TABLE advisedby_fold5_test_neg (
   student VARCHAR(32),
   advisor VARCHAR(32),
   CONSTRAINT pk_advisedby_fold5_test_neg PRIMARY KEY (student,advisor)
);

CREATE TABLE taughtby (
   course VARCHAR(32),
   professor VARCHAR(32),
   term VARCHAR(32),
   CONSTRAINT pk_taughtby PRIMARY KEY (course,professor,term)
);

CREATE TABLE courselevel (
   course VARCHAR(32),
   level VARCHAR(32),
   CONSTRAINT pk_courselevel PRIMARY KEY (course)   
);

CREATE TABLE professor (
   professor VARCHAR(32),
   pos VARCHAR(32),
   CONSTRAINT pk_professor PRIMARY KEY (professor)
);

CREATE TABLE studentinfo (
   student VARCHAR(32),
   phase VARCHAR(32),
   years VARCHAR(32),
   CONSTRAINT pk_studentinfo PRIMARY KEY (student)
);

CREATE TABLE ta (
   course VARCHAR(32),
   student VARCHAR(32),
   term VARCHAR(32),
   CONSTRAINT pk_ta PRIMARY KEY (course,student,term)
);

CREATE TABLE publication (
   publication VARCHAR(32),
   author VARCHAR(32),
   CONSTRAINT pk_publication PRIMARY KEY (publication,author)
);
