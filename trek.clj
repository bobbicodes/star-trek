(ns trek
  (:require [cheshire.core :as json]
            [clojure.string :as str]))

(defn fetch-disco [s e]
  (slurp (str "https://www.springfieldspringfield.co.uk/view_episode_scripts.php?tv-show=star-trek-discovery-2017&episode=s" s "e" e)))

(defn fetch-picard [s e]
  (slurp (str "https://www.springfieldspringfield.co.uk/view_episode_scripts.php?tv-show=star-trek-picard-2020&episode=s" s "e" e)))

(def snw
  ;; https://transcripts.foreverdreaming.org/viewtopic.php?t= ...
  {:s01e01 "72376"
   :s01e02 "72959"
   :s01e03 "73149"
   :s01e04 "73773"
   :s01e05 "73827"
   :s01e06 "74201"
   :s01e07 "74648"
   :s01e08 "75383"
   :s01e09 "75964"
   :s01e10 "76357"
   :s02e01 "116179"
   :s02e02 "116968"
   :s02e03 "118367"
   :s02e04 "119628"
   :s02e05 "120667"})

(defn episode-seq 
  "Extracts the <div> containing the episode script
   and outputs a sequence of lines."
  [html]
  (->  html
     (str/split #"<div class=\"scrolling-script-container\">")
     last
     (str/split #"</div>")
     first
     (str/split #"<br>")))

(comment
  (episode-seq (fetch-disco "01" "02"))
  (episode-seq (fetch-picard "01" "02"))
  )

(defn local-episode [series ep]
  (get-in (json/parse-string (slurp "resources/all_scripts_raw.json"))
          [series ep]))

(defn local-seq 
  "Returns a seq of alternating characters and their lines."
  [series ep]
  (interleave
   (conj (re-seq #"[A-Z]+:" (local-episode series ep)) nil)
   (str/split (local-episode series ep) #"[A-Z]+:")))

(comment
  (local-seq "TOS" "episode 0")
  )
