before running the program build the docker image:
1. go to indri-docker folder.
2. run this command: "docker build -t my-indri-app ."
3. run the container and mount a local directory to a path inside the container (e.g., /data): "docker run -it -v /path/on/local:/indri-5.11 my-indri-app"
/Users/armin/Desktop/UCI/bug-localization-project/Codes/Adjusted-BLUiR/temp