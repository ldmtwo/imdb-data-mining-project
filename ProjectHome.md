Class project for our Data Mining class.

Goal: To learn what defines a genre. We will build a set of primary (best 10 or so per genre) association rules to relate genres to keywords, plot description keywords, year, and movie title keywords. The three types of key words are fairly independent of each other.
Data sources: The data comes from IMDB (http://www.imdb.com/interfaces). Each file comes in the form of tuple lists (e.g. {movie,genre} or {movie,year}). Actual files came from this site: [ftp://ftp.sunet.se/pub/tv+movies/imdb/](ftp://ftp.sunet.se/pub/tv+movies/imdb/)
•	“Name” for each film: {title,year,[if >1 same title for that year](index.md),[if not a typical film](type.md)}
•	Genres.list: { Name, genre}
•	Keywords.list: { Name, keyword}
•	Movies.list: { Name, [episode](episode.md),[date](date.md),(beginning) year,[year](ending.md)}
•	Plots.list: {Name,plot description,author}