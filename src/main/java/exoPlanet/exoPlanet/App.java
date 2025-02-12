package exoPlanet.exoPlanet;

public class App {

	public static void main(String[] args) {
//		Thread thread = new Thread(){
//		    public void run(){
//		    	EchoMultiServer serv = new EchoMultiServer();
////				serv.start();
//		    }
//		  };
//
//		  thread.start();
//		
//		Bodenstation station = new Bodenstation("localhost", 12345, "localhost:3306", "root", "1182");
		BodenstationNew station = new BodenstationNew();
	}

}
