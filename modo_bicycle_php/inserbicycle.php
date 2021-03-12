<?php
    error_reporting(E_ALL);
    ini_set('display_errors',1);

    include('dbcon2.php');

    $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android"); 

    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android ){
    // 안드로이드 코드의 postParameters 변수에 적어준 이름을 가지고 값을 전달 받습니다.

        $bicycleID=$_POST['bicycleID'];
        $bicycleNumber=$_POST['bicycleNumber'];
        $macAddress=$_POST['macAddress'];
        $rentalPlaceLatitude=$_POST['rentalPlaceLatitude'];
        $rentalPlaceLongtitude=$_POST['rentalPlaceLongtitude'];
        $bicycleState=$_POST['bicycleState'];
        $bicycleUsage=$_POST['bicycleUsage'];
        $userID=$_POST['userID'];


        if(empty($bicycleID)){
            $errMSG = "bicycleID를 입력하세요.";
        }
        else if(empty($bicycleNumber)){
            $errMSG = "bicycleNumber를 입력하세요.";
        }
        else if(empty($macAddress)){
            $errMSG = "macAddress을 입력하세요.";
        }
        else if(empty($rentalPlaceLatitude)){
            $errMSG = "rentalPlaceLatitude을 입력하세요.";
        }
        else if(empty($rentalPlaceLongtitude)){
            $errMSG = "rentalPlaceLongtitude을 입력하세요.";
        }
        else if(empty($bicycleState)){
            $errMSG = "bicycleState을 입력하세요.";
        }
        else if(empty($bicycleUsage)){
            $errMSG = "bicycleUsage을 입력하세요.";
        }
        else if(empty($userID)){
            $errMSG = "userID을 입력하세요.";
        }

        if(!isset($errMSG)) // 이름과 나라 모두 입력이 되었다면
        {
            try{
                // SQL문을 실행하여 데이터를 MySQL 서버의 person 테이블에 저장합니다.
                $stmt = $con->prepare('INSERT INTO bicycle(bicycleID, bicycleNumber, macAddress, rentalPlaceLatitude, rentalPlaceLongtitude, bicycleState, bicycleUsage, userID) VALUES(:bicycleID, :bicycleNumber, :macAddress, :rentalPlaceLatitude, :rentalPlaceLongtitude, :bicycleState, :bicycleUsage, :userID)');
                $stmt->bindParam(':bicycleID', $bicycleID);
                $stmt->bindParam(':bicycleNumber', $bicycleNumber);
                $stmt->bindParam(':macAddress', $macAddress);
                $stmt->bindParam(':rentalPlaceLatitude', $rentalPlaceLatitude);
                $stmt->bindParam(':rentalPlaceLongtitude', $rentalPlaceLongtitude);
                $stmt->bindParam(':bicycleState', $bicycleState);
                $stmt->bindParam(':bicycleUsage', $bicycleUsage);
                $stmt->bindParam(':userID', $userID);

                if($stmt->execute())
                {
                    $successMSG = "새로운 자전거를 추가했습니다.";
                }
                else
                {
                    $errMSG = "자전거 추가 에러";
                }

            } catch(PDOException $e) {
                die("Database error: " . $e->getMessage());
            }
        }
    }

?>


<?php
    if (isset($errMSG)) echo $errMSG;
    if (isset($successMSG)) echo $successMSG;

	$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

    if( !$android )
    {
?>
    <html>
       <body>

            <form action="<?php $_PHP_SELF ?>" method="POST">
                Name: <input type = "text" name = "name" />
                Country: <input type = "text" name = "country" />
                <input type = "submit" name = "submit" />
            </form>

       </body>
    </html>

<?php
    }
?>
