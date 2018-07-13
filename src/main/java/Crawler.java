import database.DataBaseUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Crawler {
    static private DataBaseUtilities dataBaseUtilities;
    private  static Logger logger = LogManager.getLogger(Crawler.class);

    public static void main(String[] args) {
        dataBaseUtilities = new DataBaseUtilities();

        if (args.length == 1) {
            switch (args[0]) {
                case "--updatepersons":
                    updatePersons();
                    break;
                case "--updatepages":
                    updatePages();
                    break;
                case "--handlepages":
                    handlePages();
                    break;
                case "--all":
                    updatePages();
                    handlePages();
                    updatePersons();
                    break;
                default:
                    logger.warn("Wrong argument.");
            }
        }
        else logger.warn("Need one argument (--updatepersons or --updatepages or --handlepages or --all)");
    }

    static void updatePersons() {
        logger.info("Runnig update for persons:");
        dataBaseUtilities.fillPersonsPageRank();
        logger.info("End: persons are updated");
    }

    static void updatePages() {
        logger.info("Runnig updade for pages:");
        dataBaseUtilities.addRobotsTxt();
        dataBaseUtilities.addRootSitemaps();
        dataBaseUtilities.addSitemapsAndArticles();
        logger.info("End: pages are updated");
    }

    static void handlePages() {
        logger.info("Running handle for pages:");
        dataBaseUtilities.handlePages();
        logger.info("End: pages are handled");
    }
}
