import tinyq.Query;

public class EntryPoint {
    public static void main(String[] args){
        String[] strings = {
                "bla",
                "mla",
                "bura",
                "bala",
                "mura",
                "buma"
        };

        Query<String> items = (new Query<String>(strings)).selectMany(new Query.Func<String, Query<String>>(){
            public Query<String> run(String in) {
                return new Query<String>(new String[]{in + " 1", in + " 2"});
            }
        }).where(new Query.Func<String,Boolean>(){
            public Boolean run(String in) {
                return in.startsWith("b");
            }
        }).select(new Query.Func<String,String>(){
            public String run(String in) {
                return String.format("Text %s",in);
            }
        });

        System.out.println("Start: ");

        for(String s : items){
            System.out.println(s);
        }


    }
}
