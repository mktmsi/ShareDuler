<?php
ini_set('display_errors',1);

include 'DBHelper.php';

$db = new DBHelper();

$userId = $_GET['id'];
$url = $_GET['url'];
$mode = $_GET['mode'];
$distance = $_GET['distance'];
$timeMillis = $_GET['timeMillis'];

$sql = "SELECT id FROM events WHERE url = '".$url."'";
$result = mysql_query($sql);
    if (!$result) {
          die('SELECTクエリーが失敗しました。'.mysql_error());
    }
$row = mysql_fetch_assoc($result);
$event_table = "event".$row['id'];


$sql = "INSERT INTO `{$event_table}`"
  ."(id,mode,distance,timeMillis)"
  ."VALUES ($userId,$mode,$distance,$timeMillis) "
  ."ON DUPLICATE KEY UPDATE "
  ."mode = $mode, distance = $distance, timeMillis = $timeMillis;";
  $result_flag = mysql_query($sql);
  if(!$result_flag){
    die('INSERTクエリーが失敗しました'.mysql_error());
  }

?>
