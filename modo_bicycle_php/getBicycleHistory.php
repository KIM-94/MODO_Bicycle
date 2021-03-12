<?php
error_reporting(E_ALL);
ini_set('display_errors',1);

include('dbcon2.php');

//POST 값을 읽어온다.
$bicycleNumber=isset($_POST['bicycleNumber']) ? $_POST['bicycleNumber'] : '';
$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

if ($bicycleNumber != "" ){

    $sql="SELECT * FROM history WHERE bicycleNumber='$bicycleNumber'";
    $stmt = $con->prepare($sql);
    $stmt->execute();

    if ($stmt->rowCount() == 0){
      echo "'";
      echo $bicycleNumber;
      echo "'은 찾을 수 없습니다.";
    } else {
      $data = array();

      while($row=$stmt->fetch(PDO::FETCH_ASSOC)){
        extract($row);

          array_push($data,
              array(
                'userID'=>$row["userID"],
                'bicycleNumber'=>$row["bicycleNumber"],
                'rentalPlaceLatitude'=>$row["rentalPlaceLatitude"],
                'rentalPlaceLongitude'=>$row["rentalPlaceLongitude"],
                'returnPlaceLatitude'=>$row["returnPlaceLatitude"],
                'returnPlaceLongitude'=>$row["returnPlaceLongitude"],
                'rentalTime'=>$row["rentalTime"],
                'returnTime'=>$row["returnTime"],
                'useTime'=>$row["useTime"],
                'useDistance'=>$row["useDistance"]
          ));
      }

      if (!$android) {
          echo "<pre>";
          print_r($data);
          echo '</pre>';
      }

      else {
          header('Content-Type: application/json; charset=utf8');
          $json = json_encode(array("webnautes"=>$data), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
          echo $json;
      }
    }
}

else {
    echo "검색값을 입력하세요";
}

?>
