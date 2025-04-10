package examples;

public class SimpleEmuExample {
    public static void main(String[] args) {
        try {
            // Create a simple module
            SimpleModule module = new SimpleModule("TestModule");
            
            // Exercise the module's state machine
            System.out.println("\nInitial state: " + module.state());
            
            module.download();
            System.out.println("After download: " + module.state());
            
            module.prestart();
            System.out.println("After prestart: " + module.state());
            
            module.go();
            System.out.println("After go: " + module.state());
            
            module.pause();
            System.out.println("After pause: " + module.state());
            
            module.end();
            System.out.println("After end: " + module.state());
            
            module.reset();
            System.out.println("After reset: " + module.state() + "\n");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 