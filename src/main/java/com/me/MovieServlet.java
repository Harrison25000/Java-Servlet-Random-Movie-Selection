package com.me;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(name = "MovieSelect", urlPatterns = "/MovieSelect")
public class MovieServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static String charset = "UTF-8";

    static Random rand = new Random();

    static String pageNum;

    static String suppliedGenre = "Comedy";
    static String inputGenre[];
    static int genreID;
    static String idGenres;

    static String inputRating;

    static String inputVoteCount;

    static String inputYear;

    static String randomFilm;



    final static String TMDbApiKey = "b88ebacb7c48f974edc30dfadd3b43d5";

    public static final String TheMovieDB = "https://api.themoviedb.org/3/discover/movie?api_key="+TMDbApiKey+"&language=en-US&sort_by=popularity.desc&include_adult=true&include_video=false&page=1&year=2019&with_genres="+genreID;


    static HashMap<String, String> genre = new HashMap<String, String>(){
        {
            put("Action", "28");
            put("Adventure", "12");
            put("Animation", "16");
            put("Comedy", "35");
            put("Crime", "80");
            put("Documentary", "99");
            put("Drama", "18");
            put("Family", "10751");
            put("Fantasy", "14");
            put("History", "36");
            put("Horror", "27");
            put("Music", "10402");
            put("Mystery", "9648");
            put("Romance", "2107498");
            put("Science Fiction", "878");
            put("TV Movie", "10770");
            put("War", "10752");
            put("Western", "37");
        }
    };


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        out.println(" MovieServlet Executed");
        out.flush();
        out.close();
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

//        String inputActor = request.getParameter("inputActor");
//        boolean actor = inputActor.isEmpty();
//        System.out.println("Actor: "+inputActor);

        inputGenre = request.getParameterValues("inputGenre");
        String genres = "";

        inputRating = request.getParameter("inputRating");
        boolean movieRating = false;
        if(inputRating.contains("n/a")) movieRating = true;
        System.out.println("Movie Score Rating: " + inputRating);

        inputYear = request.getParameter("inputYear");
        boolean year = false;
        if(inputYear.length() != 4) year = true;
        System.out.println("Year: " + inputYear );

        inputVoteCount = request.getParameter("inputVoteCount");
        boolean voteCount = false;
        System.out.println("VoteCount: " + inputVoteCount);


        if (inputGenre != null) {
            System.out.println("Genres are: ");
            for (String genre : inputGenre) {
                genres += genre + ", ";
                System.out.println("\t" + genre);
            }
        }else{idGenres = "";
        System.out.println("No genres chosen");}



        try {
            controller(inputYear, genres, inputVoteCount, inputRating);
        } catch (Exception e) {
            e.printStackTrace();
        }


        PrintWriter writer = response.getWriter();

        // build HTML code
        String htmlResponse = "<html>";
        htmlResponse += "<h1> Your Reciept: </h1>";
        if(year == false) htmlResponse += "<h2>Chosen Year is: " + inputYear + "</h2>";
        if(genres != "") htmlResponse += "<h2>Chosen Genre/s: " + genres + "</h2>";
        htmlResponse += "<h2>Minimum Number of Ratings: " + inputVoteCount + "</h2>";
        if(movieRating == false) htmlResponse += "<h2>Chosen Movie Average Score: " + inputRating + "</h2>";
        if(randomFilm != null){ htmlResponse += "<h2> Your randomly selected film is: " + randomFilm + "<h2>";}
        else{htmlResponse += "<h2> Unfortunately We Could Not Find a Film With Your Chosen Criteria. </h2>";}
        htmlResponse += "</html>";

        // return response
        writer.println(htmlResponse);


    }

    static void controller(String inputYear, String genres, String inputVoteCount, String inputRating) throws Exception{
        ArrayList<String> filmArray = new ArrayList<String>();
        try {
            InputStream response = returnFilter(inputYear, genres, inputVoteCount, inputRating);
            InputStream randPageResponse = pageNumber(response);
            ArrayList<String> filmResults = Films(randPageResponse);
            for (String n : filmResults) {
                filmArray.add(n);
                System.out.println(n);
            }
            System.out.println("Random Movie Choice ----------");
            int filmArraySize = filmArray.size();
            int randomFilmNumber = rand.nextInt(filmArraySize);
            randomFilm = filmArray.get(randomFilmNumber);
            System.out.println(randomFilm);
            System.out.println("Done ----------");
            String randomFilmURL = "";
            if(randomFilm.contains(" ")){randomFilmURL = randomFilm.replace(" ", "%20");}
            System.out.println(randomFilmURL);
        }catch(Exception e){}

    }

    static InputStream pageNumber(InputStream response) throws IOException {

        try (Scanner scanner = new Scanner(response)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            System.out.println(responseBody);
            JSONObject filmJson = new JSONObject(responseBody);
            int totalPages = filmJson.getInt("total_pages");
                pageNum = String.valueOf(rand.nextInt(totalPages));
                int intPageNum = Integer.parseInt(pageNum);
                if(intPageNum == 0){
                    System.out.println("True");
                    pageNum = "1";
                }
            System.out.println("Random page number: " + pageNum);
        }

        URLConnection connection;
        if(idGenres == ""){ connection = new URL("https://api.themoviedb.org/3/discover/movie?api_key="+TMDbApiKey+"&language=en-US&sort_by=popularity.desc&include_adult=true&include_video=false&page="+pageNum+"&year="+inputYear+"&vote_count.gte="+inputVoteCount+"&vote_average.gte="+inputRating).openConnection();}
        else{ connection = new URL("https://api.themoviedb.org/3/discover/movie?api_key="+TMDbApiKey+"&language=en-US&sort_by=popularity.desc&include_adult=true&include_video=false&page="+pageNum+"&year="+inputYear+"&with_genres=" + idGenres + "&vote_count.gte="+inputVoteCount+"&vote_average.gte="+inputRating).openConnection();}
        System.out.println(connection);
        connection.setRequestProperty("Accept-Charset", charset);
        InputStream randPageResponse = connection.getInputStream();
        return randPageResponse;
    }

    static ArrayList<String> Films(InputStream randPageResponse){
        ArrayList<String> filmList = new ArrayList<>();

        try (Scanner scanner = new Scanner(randPageResponse)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            System.out.println(responseBody);
            JSONObject filmJson = new JSONObject(responseBody);
            String responseFilms = filmJson.get("results").toString();
            JSONArray filmJsonArray = new JSONArray(responseFilms);
            for(int i = 0; i < filmJsonArray.length(); i++){
                JSONObject film = filmJsonArray.getJSONObject(i);
                String filmTitle = film.getString("title");
                filmList.add(filmTitle);
            }
        }
        return filmList;

    }

    static InputStream returnFilter(String inYear, String genres, String inVoteCount, String inRating) throws IOException {
        InputStream response;

//        Genres -----
        idGenres = "";
        System.out.println("genres length: " + genres.length());
        if(genres.length() > 0) {
            String[] arrayGenres = genres.split(",");
            for (String x : arrayGenres) {
                idGenres += genre.get(x.trim()) + ",";
            }
            idGenres = idGenres.substring(0, idGenres.length() - 6);
            System.out.println("GenreIds: " + idGenres);
        }

//        Rating -----
        if(inRating.contains("n/a")) inputRating = "0";

//        Year -----
        if(inYear.length() != 4) inputYear = "0";

//        VoteCount -----



        URLConnection connection;
        if(idGenres == ""){ connection = new URL("https://api.themoviedb.org/3/discover/movie?api_key=" + TMDbApiKey + "&language=en-US&sort_by=popularity.desc&include_adult=true&include_video=false&page=1&year="+inputYear+"&vote_count.gte="+inputVoteCount+"&vote_average.gte="+inputRating).openConnection();}
        else{ connection = new URL("https://api.themoviedb.org/3/discover/movie?api_key=" + TMDbApiKey + "&language=en-US&sort_by=popularity.desc&include_adult=true&include_video=false&page=1&year="+inputYear+"&with_genres=" + idGenres+"&vote_count.gte="+inputVoteCount+"&vote_average.gte="+inputRating).openConnection();}
                            System.out.println(connection);
                            connection.setRequestProperty("Accept-Charset", charset);
                            response = connection.getInputStream();
        return response;
    }
}
