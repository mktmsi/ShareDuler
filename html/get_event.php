<?php
ini_set( 'display_errors', 1 );

include 'DBHelper.php';

$db =  new DBHelper();

$url = $_GET['url'];

$row = $db->get_event($url);

$jsonArray = array(
  'schedule' => $row['schedule'],
  'dateMillis' => $row['dateMillis'],
  'placeName' => $row['placeName'],
  'latitude' => $row['latitude'],
  'longitude' => $row['longitude'],
  'creatorName' => $row['creatorName']
);

header("Content-Type: application/json; charset=UTF-8");
echo json_encode($jsonArray, JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_QUOT | JSON_HEX_AMP | JSON_UNESCAPED_UNICODE);

?>
