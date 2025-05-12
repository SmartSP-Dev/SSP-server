package group4.opensource_server.group.service;

import group4.opensource_server.group.domain.*;
import group4.opensource_server.group.dto.CreateGroupRequestDto;
import group4.opensource_server.group.dto.SimpleGroupDto;
import group4.opensource_server.group.dto.TimeBlockDto;
import group4.opensource_server.group.dto.UserInfoDto;
import group4.opensource_server.group.repository.GroupMembersRepository;
import group4.opensource_server.group.repository.GroupRepository;
import group4.opensource_server.group.repository.TimeTablesRepository;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Getter
@Setter
@AllArgsConstructor
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembersRepository groupMembersRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TimeTablesRepository timeTablesRepository;

    public SimpleGroupDto createGroup(CreateGroupRequestDto requestDto) {
        /* 테스트 코드
        SimpleGroupDto newGroup = new SimpleGroupDto(1, "20250506", null);
        */
        int groupId;
        int leaderId = requestDto.getLeaderId();
        String groupKey = generateRandomKey(12);
        LocalDate createdAt = requestDto.getStartDate();
        LocalDate startDate = createdAt;
        LocalDate endDate = requestDto.getEndDate();
        LocalDate expiresAt = endDate.plusDays(3);
        String groupName = requestDto.getGroupName();

        Group newGroup = new Group(0, leaderId, groupKey, createdAt, expiresAt, startDate, endDate, groupName);
        groupRepository.save(newGroup);

        GroupMembers leaderMember = new GroupMembers(newGroup.getGroupId(), leaderId);
        groupMembersRepository.save(leaderMember);

        SimpleGroupDto responseDto = new SimpleGroupDto(newGroup.getGroupId(), newGroup.getGroupName(), newGroup.getGroupKey());

        return responseDto;
    }

    public List<UserInfoDto> getGroupMember(String groupKey) {
        List<UserInfoDto> responseDto = new ArrayList<>();

        Optional<Group> group = groupRepository.findByGroupKey(groupKey);
        int groupId = -1;

        if (group.isPresent()) {
            Group searchGroup = group.get();
            groupId = searchGroup.getGroupId();
        }
        else {
            throw new RuntimeException("입력한 그룹 키에 해당하는 그룹을 찾을 수 없습니다.");
        }

        List<GroupMembers> groupMembers = groupMembersRepository.findByGroupId(groupId);

        for (GroupMembers groupMember : groupMembers) {
            int userId = groupMember.getMemberId();

            Optional<User> user = userRepository.findById(userId);

            if (user.isPresent()) {
                User foundUser = user.get();

                UserInfoDto userInfoDto = new UserInfoDto(foundUser.getName(), foundUser.getId());
                responseDto.add(userInfoDto);
            }
            else {
                throw new RuntimeException("아이디 " + userId + "에 해당하는 사용자를 찾을 수 없습니다.");
            }
        }

        return responseDto;
    }

    public TimeBlockDto getTimeBlockWeight(String groupKey) {
        /* 테스트코드
        TimeTable 생성
        TimeTable timeTable = TimeTable.createTimeTable();

        // 예시: 타임블록에 가중치 할당
        // 실제 가중치는 비즈니스 로직에 맞게 적용해야 합니다.
        List<TimeBlock> timeBlocks = timeTable.getTimeBlocks();

        for (TimeBlock block : timeBlocks) {
            // 예시로 가중치를 0에서 10 사이로 설정
            block.setWeight((int) (Math.random() * 10));  // 예시로 랜덤 가중치 할당

            // 예시로 해당 타임블록에 선택된 사람들 할당
            if (block.getWeight() > 5) {  // 예시: 가중치가 5 이상인 블록만 선택
                block.getBlockMembers().add(new UserInfoDto("User" + block.getWeight(), block.getWeight()));
            }
        }

        // TimeBlockDto로 포장
        TimeBlockDto responseDto = new TimeBlockDto(timeBlocks);
        */
        Optional<Group> group = groupRepository.findByGroupKey(groupKey);
        if (group.isEmpty()) {
            throw new RuntimeException("입력한 그룹 키에 해당하는 그룹을 찾을 수 없습니다.");
        }

        int groupId = group.get().getGroupId();
        List<TimeTables> timeTables = timeTablesRepository.findByGroupId(groupId);

        // key: "MON-08:00", value: TimeBlock
        Map<String, TimeBlock> timeBlockMap = new HashMap<>();

        for (TimeTables i : timeTables) {
            // 유저 정보 조회
            User user = userRepository.findById(i.getMemberId())
                    .orElseThrow(() -> new RuntimeException("유저 서칭 오류"));

            UserInfoDto userInfo = new UserInfoDto(user.getName(), user.getId());

            String key = i.getDayOfWeek().toString() + "-" + i.getTimeBlock();  // 예: "MON-09:00"

            if (!timeBlockMap.containsKey(key)) {
                // 처음 보는 시간 블록이면 새로 생성
                TimeBlock timeBlock = new TimeBlock(
                        i.getDayOfWeek().toString(),
                        i.getTimeBlock(),
                        1, // 초기 weight = 1
                        new ArrayList<>(List.of(userInfo))  // 초기 blockMembers
                );
                timeBlockMap.put(key, timeBlock);
            } else {
                // 이미 존재하는 블록이면 누적
                TimeBlock existingBlock = timeBlockMap.get(key);
                existingBlock.setWeight(existingBlock.getWeight() + 1);
                existingBlock.getBlockMembers().add(userInfo);
            }
        }

        // Map의 value들을 리스트로 만들어 Dto로 포장
        List<TimeBlock> resultList = new ArrayList<>(timeBlockMap.values());
        return new TimeBlockDto(resultList);
    }

    private String generateRandomKey(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();

        int maxAttempts = 100;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }

            String key = sb.toString();

            if (groupRepository.findByGroupKey(key).isEmpty()) {
                return key;
            }
        }

        throw new RuntimeException("랜덤 키 생성 실패: 중복된 키가 너무 많습니다.");
    }


}
