public class Main {
    public static void main(String[] args) {

        LibrarySystem system=new LibrarySystem();
        LibraryGUI gui=new LibraryGUI(system);

        gui.openLoginDialog(); //open login
    }
}
