(ns uni-webscraper.util-func
 (:require [clojure.string :as str]))

(defn split-params [params] 
  (map #(str.split %) (str.split params #"&") #"="))
