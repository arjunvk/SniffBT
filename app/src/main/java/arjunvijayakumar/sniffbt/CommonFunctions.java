package arjunvijayakumar.sniffbt;

public class CommonFunctions {

    /**
     * Method to put application in sleep for specified number of seconds
     * @param intSleepTimeInSeconds - The {@link int} number of seconds
     */
    public void sleepForNSeconds(int intSleepTimeInSeconds){
        try {
            Thread.sleep(intSleepTimeInSeconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
