import java.util.*;

public class Test6 {

    public static void main(String[] args) {

        List<String> list = Arrays.asList("001001" ,"001001" ,"001001" ,"001002" ,"001002" ,"001002" ,"001003" ,"001003" ,"001003" ,"002001" ,"002001" ,"002001" ,"002001" ,"002001" ,"002001" ,"002001" ,"002001" ,"002008" ,"002008" ,"002002" ,"002002" ,"002002" ,"002003" ,"002003" ,"002006" ,"002006" ,"002007" ,"002007" ,"002007" ,"002007" ,"002007" ,"002007" ,"002005" ,"002005" ,"002005" ,"003001" ,"003001" ,"003001" ,"003001" ,"003001" ,"003001" ,"003001" ,"003001" ,"003001" ,"003001" ,"003003" ,"003003" ,"003003" ,"003003" ,"003003" ,"003003" ,"003005" ,"003005" ,"003005" ,"003005" ,"003005" ,"003005" ,"003005" ,"003005" ,"003006" ,"003006" ,"003006" ,"003006" ,"003006" ,"003006" ,"003006" ,"003006" ,"003004" ,"003004" ,"003004" ,"003004" ,"003004" ,"003004" ,"003004" ,"003004" ,"003004" ,"003004" ,"003004" ,"003004" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004003" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004004" ,"004003" ,"004003" ,"004003" ,"003007" ,"003007" ,"003007" ,"004006" ,"004006" ,"004006" ,"004006" ,"004006" ,"004006" ,"004006" ,"008001" ,"008001" ,"008001" ,"008001" ,"008001" ,"008001" ,"008001" ,"008001" ,"004001" ,"004002" ,"004002" ,"004002" ,"004002" ,"004002" ,"005011" ,"005011" ,"005011" ,"005011" ,"005011" ,"005011" ,"005011" ,"005011" ,"005009" ,"005009" ,"005009" ,"005009" ,"005009" ,"005009" ,"005009" ,"005003" ,"005003" ,"005003" ,"005003" ,"005003" ,"005003" ,"005003" ,"005003" ,"005003" ,"005003" ,"005005" ,"005005" ,"005005" ,"005005" ,"005005" ,"005005" ,"005005" ,"005005" ,"005005" ,"005005" ,"005006" ,"005006" ,"005006" ,"005006" ,"005006" ,"005006" ,"005006" ,"005006" ,"005006" ,"005006" ,"006001" ,"006001" ,"005012" ,"005012" ,"005012" ,"005012" ,"005012" ,"005012" ,"005012" ,"005012" ,"005008" ,"005008" ,"005008" ,"005008" ,"005008" ,"005008" ,"005008" ,"005010" ,"005010" ,"005010" ,"005004" ,"005004" ,"005004" ,"005004" ,"005004" ,"005004" ,"005010" ,"005001" ,"005001" ,"005001" ,"005002" ,"005002" ,"005002" ,"005002" ,"005002" ,"005002" ,"005002" ,"005002" ,"005007" ,"005007" ,"005007" ,"005007" ,"005007" ,"005007" ,"005007" ,"005007" ,"007001" ,"007001" ,"008003" ,"008003" ,"008003" ,"008002" ,"008002" ,"008002");
        List<String> list1 = Arrays.asList("001001" ,"001002" ,"001003" ,"002001" ,"002008" ,"002002" ,"002003" ,"002004" ,"002005" ,"002006" ,"002007" ,"003001" ,"003002" ,"003004" ,"003003" ,"003005" ,"003006" ,"003007" ,"004001" ,"004002" ,"004003" ,"004004" ,"004005" ,"004006" ,"004007" ,"005001" ,"005002" ,"005003" ,"005004" ,"005005" ,"005006" ,"005007" ,"005008" ,"005009" ,"005010" ,"005011" ,"005012" ,"006001" ,"007001" ,"008001" ,"008002" ,"008003");

        for (String str : list1){
           if (!list.contains(str)){
               System.out.println(str);
           }
        }

    }


}
