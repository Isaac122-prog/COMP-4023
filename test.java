public class test {

    public static void main(String[] args) {
        System.out.println("Regex Engine");
    }

    static class State {
        int id;
        boolean accepting;

        State(int id, boolean accepting){
            this.id = id;
            this.accepting = accepting;
        }
        
    }
}