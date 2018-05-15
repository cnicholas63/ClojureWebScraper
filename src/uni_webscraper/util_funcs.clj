; Author: Chris Nicholas
; Date 11/4/17
; Util functions.
(ns uni-webscraper.util_funcs
  (:require 
            [clojure.string :as str]))


; Splits a raw parameter query, e.g.:
;   search=Rangerover&distance=15&postcode=L202DF
; into a list of vectors into pairs, e.g.: 
;   [search Rangerover]
(defn split-params [params]
  (map #(str/split % #"=") (str/split params #"&")))

; Converts param list to strings
(defn stringify [params]
  (map #(str (quote %)) params)) 

; Converts a tuple (vector) into key/value pair, 
; e.g: [a 2] -> [:a 2]
(defn keyval [[mykey myval]] 
  (vector (keyword (str mykey)) (str myval)))

; Converts vector of pairs into hashmap of key/value pairs
; e.g: [:a 2 :b 4 :c 6] -> {:a 2 :b 4 :c 6}
(defn vect-map [vect] 
  (into {} (map keyval vect)))
