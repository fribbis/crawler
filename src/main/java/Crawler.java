import database.DataBaseUtilities;

public class Crawler {
    static private DataBaseUtilities dataBaseUtilities;

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
                    System.out.println("Wrong argument.");
            }
        }
        else System.out.println("Need one argument (--updatepersons or --updatepages or --handlepages or --all)");
    }

    static void updatePersons() {
        System.out.println("Runnig update for persons:");
        dataBaseUtilities.fillPersonsPageRank();
        System.out.println("End: persons are updated");
    }

    static void updatePages() {
        System.out.println("Runnig updade for pages:");
        dataBaseUtilities.addRobotsTxt();
        dataBaseUtilities.addRootSitemaps();
        dataBaseUtilities.addSitemapsAndArticles();
        System.out.println("End: pages are updated");
    }

    static void handlePages() {
        System.out.println("Running handle for pages:");
        dataBaseUtilities.handlePages();
        System.out.println("End: pages are handled");
    }
}
