(defproject comicdog "0.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [http-kit "2.2.0"]
                 [clj-http "2.3.0"]
                 [enlive "1.1.6"]
                 [clojurewerkz/urly "1.0.0"]
                 [org.clojure/tools.cli "0.3.1"]]
  :plugins [[cider/cider-nrepl "0.14.0"]]
  :main comicdog.core)
