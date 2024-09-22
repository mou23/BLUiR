before running the program build the docker image:
1. go to indri-docker folder.
2. run this command: "docker build -t my-indri-app ."
3. run the container and mount a local directory to a path inside the container (e.g., /data): "docker run -it -v /path/on/local:/indri-5.11 my-indri-app"
/Users/armin/Desktop/UCI/bug-localization-project/Codes/Adjusted-BLUiR/temp


first make index
docker run --platform linux/amd64 --name temp_index_container -v /Users/armin/Desktop/UCI/bug-localization-project/Codes/Adjusted-BLUiR/data/temp/BLUiR_first_Run/docs:/docs -v /Users/armin/Desktop/UCI/bug-localization-project/Codes/Adjusted-BLUiR/stopwords:/stopwords -v /Users/armin/Desktop/UCI/bug-localization-project/Codes/Adjusted-BLUiR/fields:/fields roynirmal/pyndri IndriBuildIndex -corpus.path=/docs -corpus.class=trectext -index=/index -memory=2000M -stemmer.name=Krovetz -stopper.word=/stopwords -fields=/fields

docker run --platform linux/amd64 --name temp_index_container -v /Users/armin/Desktop/UCI/bug-localization-project/Codes/Adjusted-BLUiR/data/temp/BLUiR_first_Run/query:/query -v /Users/armin/Desktop/UCI/bug-localization-project/Codes/Adjusted-BLUiR/data/temp/BLUiR_first_Run/docs:/docs -v /Users/armin/Desktop/UCI/bug-localization-project/Codes/Adjusted-BLUiR/stopwords:/stopwords -v /Users/armin/Desktop/UCI/bug-localization-project/Codes/Adjusted-BLUiR/fields:/fields -d roynirmal/pyndri /bin/bash -c "IndriBuildIndex -corpus.path=/docs -corpus.class=trectext -index=/index -memory=2000M -stemmer.name=Krovetz -stopper.word=/stopwords -fields=/fields && IndriRunQuery /query -count=100 -index=/index -trecFormat=true -rule=method:tfidf,k1:1.0,b:0.3 && tail -f /dev/null"
