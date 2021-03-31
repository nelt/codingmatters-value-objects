package org.codingmatters.value.objects.demo.books;

import org.codingmatters.value.objects.demo.books.person.Address;
import org.codingmatters.value.objects.demo.books.review.ReviewRating;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
/**
 * Created by nelt on 11/26/16.
 */
public class BookTest {

    public static final DateTimeFormatter ENGLISH_DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("MMMM d, uuuu")
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter(Locale.ENGLISH)
            ;

    @Test
    public void chainedBuilderAndCompleteToString() throws Exception {
        Book cleanCode = Book.builder()
                .name("Clean Code: A Handbook of Agile Software Craftsmanship")
                .author(Person.builder()
                        .name("Robert C. Martin")
                        .build())
                .bookFormat("Paperback")
                .kind(Book.Kind.TEXTBOOK)
                .tags(Book.Tags.LITERATURE, Book.Tags.SCIENCE)
                .datePublished(LocalDate.parse("August 11, 2008", ENGLISH_DATE_FORMATTER))
                .isbn("978-0132350884")
                .numberOfPages(464)
                .reviews(
                        Review.builder()
                                .author(Person.builder()
                                        .name("John Doe")
                                        .build())
                                .datePublished(LocalDateTime.parse("September 23, 2008", ENGLISH_DATE_FORMATTER))
                                .reviewBody(
                                        "I enjoyed reading this book and after finishing it, " +
                                                "I decided to apply the Boy Scout Rule."
                                )
                                .reviewRating(ReviewRating.builder()
                                        .ratingValue(5)
                                        .build())
                                .build()
                )
                .build();


        assertThat(cleanCode.toString(), isOneOf(
                "Book{" +
                        "name=Clean Code: A Handbook of Agile Software Craftsmanship, " +
                        "author=Person{" +
                            "name=Robert C. Martin, email=null, address=null" +
                        "}, " +
                        "bookFormat=Paperback, " +
                        "datePublished=2008-08-11, " +
                        "kind=TEXTBOOK, " +
                        "tags=[LITERATURE, SCIENCE], " +
                        "isbn=978-0132350884, " +
                        "numberOfPages=464, " +
                        "reviews=[Review{" +
                            "author=Person{name=John Doe, email=null, address=null}, " +
                            "datePublished=2008-09-23T00:00, " +
                            "itemReviewed=null, " +
                            "reviewBody=I enjoyed reading this book and after finishing it, I decided to apply the Boy Scout Rule., " +
                            "reviewRating=ReviewRating{ratingValue=5}" +
                        "}]" +
                    "}",
                "Book{" +
                        "name=Clean Code: A Handbook of Agile Software Craftsmanship, " +
                        "author=Person{" +
                            "name=Robert C. Martin, email=null, address=null" +
                        "}, " +
                        "bookFormat=Paperback, " +
                        "datePublished=2008-08-11, " +
                        "kind=TEXTBOOK, " +
                        "tags=[SCIENCE, LITERATURE], " +
                        "isbn=978-0132350884, " +
                        "numberOfPages=464, " +
                        "reviews=[Review{" +
                            "author=Person{name=John Doe, email=null, address=null}, " +
                            "datePublished=2008-09-23T00:00, " +
                            "itemReviewed=null, " +
                            "reviewBody=I enjoyed reading this book and after finishing it, I decided to apply the Boy Scout Rule., " +
                            "reviewRating=ReviewRating{ratingValue=5}" +
                        "}]" +
                        "}"
        ));

        System.out.println(cleanCode.toMap());

        assertThat(Book.fromMap(cleanCode.toMap()).build(), is(cleanCode));
    }

    @Test
    public void equalityOnValue() throws Exception {
        Person sameValueAsJohn = Person.builder()
                .name("John Doe")
                .build();
        Person john = Person.builder()
                .name("John Doe")
                .build();
        Person jane = Person.builder()
                .name("Jane Doe")
                .build();

        assertThat(john, is(sameValueAsJohn));
        assertThat(jane, is(not(sameValueAsJohn)));
    }

    @Test
    public void singlePropertyChange() throws Exception {
        Person john = Person.builder()
                .name("John")
                .address(Address.builder()
                        .postalCode("25000")
                        .build())
                .build();

        Person changed = john.withEmail("john@doe.com");

        assertThat("value as changed          ", changed, is(not(john)));
        assertThat("email property has changed", changed.email(), is("john@doe.com"));
        assertThat("others are left unchanged ", changed.name(), is(john.name()));
        assertThat("others are left unchanged ", changed.address(), is(john.address()));
    }

    @Test
    public void bulkChange() throws Exception {
        Person john = Person.builder()
                .name("John")
                .address(Address.builder()
                        .postalCode("25000")
                        .build())
                .build();


        Person changed = john.changed(builder -> builder
                .name(john.name() + " Doe")
                .email("john@doe.com")
        );

        assertThat("value as changed          ", changed, is(not(john)));
        assertThat("name has changed          ", changed.name(), is("John Doe"));
        assertThat("email property has changed", changed.email(), is("john@doe.com"));
        assertThat("others are left unchanged ", changed.address(), is(john.address()));
    }

    @Test
    public void consumerBuilder() throws Exception {
        Book book = Book.builder()
                .author(author -> author.name("Arthur Miller"))
                .reviews(
                        reviews -> reviews.reviewBody("quite good"),
                        reviews -> reviews.reviewBody("very good"),
                        reviews -> reviews.reviewBody("quite nice")
                )
                .build();

        assertThat(book.author().name(), is("Arthur Miller"));
        assertThat(book.reviews().size(), is(3));
        assertThat(book.reviews().get(0).reviewBody(), is("quite good"));
        assertThat(book.reviews().get(1).reviewBody(), is("very good"));
        assertThat(book.reviews().get(2).reviewBody(), is("quite nice"));
    }

    @Test
    public void formattedStringSetter() throws Exception {
        assertThat(Person.builder().name("%s %s", "Arthur", "Miller").build().name(), is("Arthur Miller"));
    }

    @Test
    public void changedNestedProperty() throws Exception {
        Book book = Book.builder()
                .author(author -> author.name("Arthur Miller"))
                .build();

        Book changed = book.withChangedAuthor(author -> author.email("arthur@miller.com"));

        assertThat(changed.author(), is(Person.builder().name("Arthur Miller").email("arthur@miller.com").build()));
    }

    @Test
    public void changeNestedCollectionProperty() throws Exception {
        Book book = Book.builder()
                .reviews(
                        reviews -> reviews.reviewBody("bad"),
                        reviews -> reviews.reviewBody("very good"),
                        reviews -> reviews.reviewBody("awful")
                )
                .build();

        Book changed = book.withChangedReviews(reviews -> reviews.filtered(review -> review.reviewBody().contains("good")));

        assertThat(changed.reviews(), contains(Review.builder().reviewBody("very good").build()));
    }

    @Test
    public void filterAValueList() throws Exception {
        Book book = Book.builder()
                .reviews(
                        reviews -> reviews.reviewBody("bad"),
                        reviews -> reviews.reviewBody("very good"),
                        reviews -> reviews.reviewBody("awful")
                )
                .build();

        Book changed = book.withReviews(ValueList.from(book.reviews()).filtered(review -> review.reviewBody().contains("good")).build());

        assertThat(changed.reviews(), contains(Review.builder().reviewBody("very good").build()));
    }
}