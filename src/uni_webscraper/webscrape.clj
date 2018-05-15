; Author: Chris Nicholas
; Date 12/4/17
; This file performs web scraping operation
; Before the results are sent for processing
; Function webscrape (bottom of page) is main entry point.

(ns uni-webscraper.webscrape
  (:require 
   [org.httpkit.client :as http]
   [net.cgrand.enlive-html :as html]
   [uni-webscraper.util_funcs :as utils]
   [clojure.string :as str]
   ))

; Key/value pairs for URI encoded search
; The keys match against the hashmap from 
; user specified search.
; The beauty about using a map is that the order of
; keys doesnt matter whan matching one to the other
; Also it's easy to ignore unspecified parameters,
; for example if no price range is specified.
(def search-keys {:search "_nkw"
                  :pricelow "_udlo"
                  :pricehigh "_udhi"
                  :postcode "_stpos"
                  :distance "_sadis"})

; Base url for search, _ipg=200 limits to 200 results
(def base-url "http://www.ebay.co.uk/sch/i.html?_ipg=50")


; Used when tracking realised futures
(def realised-count (atom 0))


; Builds a query string ready to be passed as part of URI
; Params contains map of parameter parts
(defn build-query [params]
  ; run through key value pairs selecting only those with non-blank entries
  (let [filtered (filter  #(not (str/blank? (get params %))) (keys params))]
    (str base-url (apply str (map #(str "&" (get search-keys %) "=" (get params %)) filtered)))))



; Retrieves data from url, this requires different selectors
; to that of the parent - search page
(defn extract-titles-new
  [dom] 
  (let [page-data (html/select dom [:h1#itemTitle.it-ttl])]
     (second (get (first page-data) :content))))


; Extract titles from DOM that have :h3.lvtitle and :a
(defn extract-titles
    [dom]
    (map
     (comp first :content) (html/select dom [[:h3.lvtitle (html/has [:a])]]))) 


; Retrieve DOM from specified URL
(defn get-dom 
  [url]
  (html/html-snippet
   (:body @(http/get url {:insecure? true}))))


; Extract the required parts from web page: URL 
; get-in allows retrieval of information from nested hash-maps
(defn getvals
  [url]
  (let [results (extract-titles (get-dom url))] 
    (map #(get-in % [:attrs :href]) results)
     ;(get-in (first results) [:attrs :href])
    ))


; Dispatches a thread via future based on param
; This extracts the title from the target web page
(defn threads-dispatch [param]
  (future (extract-titles-new (get-dom param))))

; Tests if promises have been realised
(defn threads-realised [param]
  (println "Testing")
  (if (realized? param)
    (do
      (swap! realised-count inc)
      (println "Realised!")
      (println @param)))
)

; future-control should manage a list of futures
; dispatching threads 1:1 against list contents.
; The threads do dispatch and are retained in the atom threads
; However, I have not been able to retrieve the results as threads
; promises are realised. The code represents the solution as far as I 
; got - This does not function correctly
; It also feels very procedural.
(defn future-control
  [list]
  (def threads (atom ()))
  (def counter (atom 0))
  (def inner-loop (atom 0))

  ; Filter out any nil links - we don't want to process them
  (def links (filter #(not (nil? %)) list))
  (reset! realised-count 0)

  (println (str "Links in list = " (count list)))

  ; dispatch threads, retaining references in atom threads
  (while (< @counter (count list))
    (swap! threads conj threads-dispatch (nth links @counter))
    (swap! counter inc))

  (print "Threads dispatched: " )
  (println @counter)
  (println (count @threads))
  (println (str "realised-count: " @realised-count ", counter: " @counter))


  (while (< @realised-count @counter)
    (println (map threads-realised threads))
    (println (str "@realised-count: " @realised-count)))
)


; Main entry point, this recieves a query from server
; with parameters in the form of an unprocessed query string e.g.: 
;   search=Rangerover&distance=15&postcode=L202DF
; This is processed and used for web scraping operations
; This includes not very idiomatic defs - this is to accommodate all the printlns
(defn webscrape [query] 
  (let [params (utils/split-params query)]
       (println "webscrape query:")
       (println params)
       (def query-str (build-query (utils/vect-map params)))
       (println (str "Searching: " query-str))
       (def result-set (getvals query-str))
       (println (str "Result count = " (count result-set))) 
       
       ; future-control should provide a concurrent solution
       ; using threads via future - however, I have struggled to make it work
       ;(future-control result-set)
       
       ; Alternative solution, this just sequentially processes the hyperlinks
       (doall (map #(println (extract-titles-new (get-dom %))) result-set))
       
       ; Conformation message indicating processes finished
       (println "Hello I'm done")))






