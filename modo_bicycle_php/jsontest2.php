<?php
$json = '{
    "title": "PHP",
    "site": "GeeksforGeeks"
}';
 
$json2  = '{
    "webnautes": [
        {
            "bicycleNumber": "bicycle:3",
            "macAddress": "98:D3:31:FD:34:86",
            "rentalPlaceLatitude": "37.2965003",
            "rentalPlaceLongtitude": "126.8185463",
            "bicycleState": "available"
        },
        {
            "bicycleNumber": "bicycle:4",
            "macAddress": "98:d3:31:fd:34:81",
            "rentalPlaceLatitude": "37.306238",
            "rentalPlaceLongtitude": "126.832973",
            "bicycleState": "available"
        },
        {
            "bicycleNumber": "bicycle:5",
            "macAddress": "98:D3:31:FD:34:80",
            "rentalPlaceLatitude": "37.297526",
            "rentalPlaceLongtitude": "126.818424",
            "bicycleState": "available"
        },
        {
            "bicycleNumber": "bicycle:6",
            "macAddress": "98:D3:31:FD:34:85",
            "rentalPlaceLatitude": "37.224615",
            "rentalPlaceLongtitude": "127.187907",
            "bicycleState": "available"
        },
        {
            "bicycleNumber": "bicycle:7",
            "macAddress": "98:D3:31:FS:12:45",
            "rentalPlaceLatitude": "37.421998",
            "rentalPlaceLongtitude": "122.084",
            "bicycleState": "available"
        }
    ]
}';

$data = json_decode($json);

echo $data->title;
echo "\n";

echo $data->site;


$data2 = json_decode($json2, true);
$result = printValues($data2);
//echo $data2["webnautes"][0]["bicycleNumber"];

echo $data2->webnautes;
echo "\n";

//echo $data->site;

?>
