![alt text](https://raw.githubusercontent.com/dilentulcidas/the-hangman-game/master/githubimages/hangmanheader.png "Hangman Header")

![alt text](https://raw.githubusercontent.com/dilentulcidas/the-hangman-game/master/githubimages/hangmanscreenshots.png "Hangman screenshots")

# Description
This is the source code for simple Hangman game using Java and JavaFX powering the user interface. The user also has the additional option of saving the game or not.

At the beginning the user has a choice of choosing either "Sequential" or "Parallel" mode. This is for personal testing purposes to check when the program runs with multiple threads if it's faster than running in a single thread. For that, it selects random words from each of the four txt files (full of words) and serializes it into one file (containing all the random words selected from the four txt files), of which these words will then be used to be guessed by the user in the game. 

In this case, running the operation mentioned above via multithreading ends up being slightly slower than single thread sometimes due to context switching and the operations not being that expensive to the cpu, as the text files are not that huge in size.

The game can be run from the .jar file in the release folder on the root of this project.

**Last update:** 3rd January 2017
