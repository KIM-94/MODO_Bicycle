<?php
error_reporting(E_ALL);
ini_set('display_errors',1);

include('dbcon2.php');

//POST 값을 읽어온다.
$userID=isset($_POST['userID']) ? $_POST['userID'] : '';
$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

if ($userID != "" ){

    $sql="SELECT * FROM bicycle WHERE userID='$userID'";
    $stmt = $con->prepare($sql);
    $stmt->execute();

    if ($stmt->rowCount() == 0){
      echo "'";
      echo $bicycleState;
      echo "'은 찾을 수 없습니다.";
    } else {
      $data = array();

      while($row=$stmt->fetch(PDO::FETCH_ASSOC)){
        extract($row);
        array_push($data,
            array(
              'bicycleNumber'=>$row["bicycleNumber"],
              'macAddress'=>$row["macAddress"],
              'rentalPlaceLatitude'=>$row["rentalPlaceLatitude"],
              'rentalPlaceLongtitude'=>$row["rentalPlaceLongtitude"],
              'bicycleState'=>$row["bicycleState"],
              'userID'=>$row["userID"],
        ));
      }

      header('Content-Type: application/json; charset=utf8');
      $json = json_encode(array("webnautes"=>$data), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
      echo $json;
    }
}

else {
    echo "검색값을 입력하세요";
}

?>
