package group4.opensource_server.group.service;

import group4.opensource_server.group.domain.*;
import group4.opensource_server.group.dto.SuccessResponseDto;
import group4.opensource_server.group.dto.TimeBlockDto;
import group4.opensource_server.group.dto.UserInfoDto;
import group4.opensource_server.group.repository.GroupMembersRepository;
import group4.opensource_server.group.repository.GroupRepository;
import group4.opensource_server.group.repository.TimeTablesRepository;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Getter
@Setter
@AllArgsConstructor
@Transactional
public class MemberService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembersRepository groupMembersRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TimeTablesRepository timeTablesRepository;

    public SuccessResponseDto enterGroup(String groupKey, UserInfoDto user) {
        SuccessResponseDto responseDto;

        Optional<Group> optionalGroup = groupRepository.findByGroupKey(groupKey);
        Group group;

        if (optionalGroup.isPresent()) {
            group = optionalGroup.get();
        }
        else {
            throw new RuntimeException("해당 그룹 키를 가진 그룹이 없습니다.");
        }

        GroupMembers newMember = new GroupMembers(group.getGroupId(), user.getId());

        boolean alreadyJoined = groupMembersRepository.existsByGroupIdAndMemberId(newMember.getGroupId(), newMember.getMemberId());
        if (alreadyJoined) {
            throw new RuntimeException("이미 이 그룹에 가입되어 있습니다.");
        }

        groupMembersRepository.save(newMember);
        responseDto = new SuccessResponseDto(true);

        return responseDto;
    }

    public SuccessResponseDto registerTimeTable(String groupKey, TimeBlockDto timeTableDto, int userId) {
        //System.out.println("[registerTimeTable] Called with groupKey = " + groupKey + ", userId = " + userId);

        Group group = groupRepository.findByGroupKey(groupKey)
                .orElseThrow(() -> new RuntimeException("해당 그룹 키를 가진 그룹이 없습니다."));
        int groupId = group.getGroupId();
        //System.out.println("[registerTimeTable] Found groupId = " + groupId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 사용자가 존재하지 않습니다."));
        //System.out.println("[registerTimeTable] Found user = " + user.getName());

        List<TimeTables> existingTimeTables = timeTablesRepository.findByGroupId(groupId);
        //System.out.println("[registerTimeTable] Found existing timetables: " + existingTimeTables.size());

        LocalDate startDate;
        LocalDate endDate;

        if (!existingTimeTables.isEmpty()) {
            startDate = existingTimeTables.get(0).getStartDate();
            endDate = existingTimeTables.get(0).getEndDate();
        } else {
            startDate = group.getStartDate();
            endDate = group.getEndDate();
        }

        int deleted = timeTablesRepository.deleteByGroupIdAndMemberId(groupId, userId);
        //System.out.println("[registerTimeTable] Deleted previous timetable entries = " + deleted);

        // user name이 null이면 임시 이름 부여
        if (user.getName() == null) {
            user.setName("tempUser");
        }

        UserInfoDto currentUser = new UserInfoDto(user.getName(), userId);

        for (TimeBlock block : timeTableDto.getTimeBlocks()) {
            // ★ 서버에서 blockMembers를 직접 생성 및 주입
            List<UserInfoDto> blockMembers = new ArrayList<>();
            blockMembers.add(currentUser);
            block.setBlockMembers(blockMembers);

            // 이후 기존 로직 그대로 사용
            boolean userSelected = block.getBlockMembers().stream()
                    .anyMatch(member -> member.getId() == userId);

            //System.out.println("[registerTimeTable] Checking block " + block.getDayOfWeek() + "-" + block.getTime() +
            //        ", userSelected = " + userSelected);

            if (!userSelected) continue;

            TimeTables entry = new TimeTables();
            entry.setGroupId(groupId);
            entry.setMemberId(userId);
            entry.setDayOfWeek(DayOfWeekEnum.valueOf(block.getDayOfWeek()));
            entry.setTimeBlock(block.getTime());
            entry.setWeight(1); // 필요한 경우 가중치 변경 가능
            entry.setStartDate(startDate);
            entry.setEndDate(endDate);
            entry.setAvailable(true);

            timeTablesRepository.saveAndFlush(entry);
            //System.out.println("[registerTimeTable] Saved entry: " + entry.getDayOfWeek() + "-" + entry.getTimeBlock());
        }

        return new SuccessResponseDto(true);
    }








    public SuccessResponseDto updateTimeTable(String groupKey, TimeBlockDto timeTableDto, int userId) {
        // 1. 그룹 조회
        Group group = groupRepository.findByGroupKey(groupKey)
                .orElseThrow(() -> new RuntimeException("해당 그룹 키를 가진 그룹이 없습니다."));
        int groupId = group.getGroupId();

        // 1-1. 사용자 정보 조회 및 Dto 변환
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 ID의 사용자가 존재하지 않습니다."));
        UserInfoDto userInfoDto = new UserInfoDto(user.getName(), user.getId());

        // 2. 기존 그룹의 전체 시간표 조회
        List<TimeTables> existingTimeTables = timeTablesRepository.findByGroupId(groupId);

        // 3. 기존 상태 재구성: "MON-08:00" -> Set<userId>
        Map<String, Set<Integer>> blockUserMap = new HashMap<>();
        LocalDate startDate = null;
        LocalDate endDate = null;

        // 기존 시간표에서 startDate와 endDate를 가져옴 (예시로 첫 번째 시간표에서 값 사용)
        if (!existingTimeTables.isEmpty()) {
            TimeTables firstEntry = existingTimeTables.get(0);
            startDate = firstEntry.getStartDate(); // 첫 번째 시간표의 startDate
            endDate = firstEntry.getEndDate();     // 첫 번째 시간표의 endDate
        }

        // 4. 자기 시간표 삭제
        timeTablesRepository.deleteByGroupIdAndMemberId(groupId, userId);

        // 5. 사용자가 선택한 블록마다 저장
        for (TimeBlock block : timeTableDto.getTimeBlocks()) {
            boolean userSelected = block.getBlockMembers()
                    .stream()
                    .anyMatch(member -> member.getId() == userId);

            if (!userSelected) continue;

            String key = block.getDayOfWeek() + "-" + block.getTime().toString();

            // 가중치 계산
            int weight = blockUserMap.getOrDefault(key, new HashSet<>()).size() + 1;

            // blockMembers 업데이트
            Set<Integer> members = blockUserMap.getOrDefault(key, new HashSet<>());
            members.add(userId);
            blockUserMap.put(key, members);

            // DB 저장
            TimeTables entry = new TimeTables();
            entry.setGroupId(groupId);
            entry.setMemberId(userId);
            entry.setDayOfWeek(DayOfWeekEnum.valueOf(block.getDayOfWeek()));
            entry.setTimeBlock(block.getTime());
            entry.setWeight(weight);

            // 기존 시간표의 startDate와 endDate 설정
            entry.setStartDate(startDate);  // 기존 시간표의 startDate
            entry.setEndDate(endDate);      // 기존 시간표의 endDate

            timeTablesRepository.save(entry);
        }

        return new SuccessResponseDto(true);
    }

    public TimeBlockDto getUserTimeBlock(String groupKey, int userId) {
        TimeBlockDto responseDto = new TimeBlockDto();

        Group group = groupRepository.findByGroupKey(groupKey)
                .orElseThrow(() -> new RuntimeException("해당 그룹 키를 가진 그룹이 없습니다."));
        int groupId = group.getGroupId();

        List<TimeTables> timeTables = timeTablesRepository.findByGroupIdAndMemberId(groupId, userId);

        List<TimeBlock> resultList = new ArrayList<>();

        for (TimeTables table : timeTables) {
            TimeBlock block = new TimeBlock(
                    table.getDayOfWeek().name(),
                    table.getTimeBlock(),
                    table.getWeight(),
                    null
            );

            resultList.add(block);
        }

        for (TimeBlock block : resultList) {
            block.setBlockMembers(Collections.emptyList());
        }

        responseDto.setTimeBlocks(resultList);

        return responseDto;
    }


}
