public class Main {
    public static void main(String[] args) {
        String key = "test/jabberwocky/5";
        String name = "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-";
        String address = "10.0.0.164";
        int port = 20000;

        for(int i = 0; i < 11 ; i++){
            try{
                int newPort = port + i;
                byte[] keyHash = HashID.computeHashID(key+"\n");
                byte[] nameHash = HashID.computeHashID(name + newPort+"\n");


                System.out.println("I = "+ i +" : "+HashID.calculateDistance(keyHash,nameHash));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
