-- Seed large learning dataset aligned with assessment-service seed.
-- 10 courses, each 2-3 sections, each section 2 lessons.
-- Each lesson includes multi-type blocks, including short-answer.

BEGIN;

CREATE OR REPLACE FUNCTION seeded_uuid(namespace_text text, seq_num bigint)
RETURNS uuid
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT (
        substr(md5(namespace_text || ':' || seq_num::text), 1, 8) || '-' ||
        substr(md5(namespace_text || ':' || seq_num::text), 9, 4) || '-' ||
        substr(md5(namespace_text || ':' || seq_num::text), 13, 4) || '-' ||
        substr(md5(namespace_text || ':' || seq_num::text), 17, 4) || '-' ||
        substr(md5(namespace_text || ':' || seq_num::text), 21, 12)
    )::uuid;
$$;

DO $$
DECLARE
    c_idx int;
    s_idx int;
    l_idx int;
    section_total int;
    lesson_key int;
    course_id_v uuid;
    section_id_v uuid;
    lesson_id_v uuid;
    assessment_id_v uuid;
    q_mcq_id uuid;
    q_fill_id uuid;
    q_short_id uuid;
    q_match_id uuid;
    q_order_id uuid;
    q_hotspot_id uuid;
    demo_user_id uuid := 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid;
BEGIN
    FOR c_idx IN 1..10 LOOP
        course_id_v := seeded_uuid('course', c_idx);
        section_total := CASE WHEN mod(c_idx, 2) = 0 THEN 3 ELSE 2 END;

        INSERT INTO course (
            id, name, slug, description, icon_file_name, color_code, created_at, updated_at
        ) VALUES (
            course_id_v,
            format('Emergency Program %s', c_idx),
            format('emergency-program-%s', c_idx),
            format('Auto-seeded course %s for QA and demo.', c_idx),
            null,
            '#2aa4e8',
            now(),
            now()
        )
        ON CONFLICT (id) DO UPDATE SET
            name = EXCLUDED.name,
            slug = EXCLUDED.slug,
            description = EXCLUDED.description,
            color_code = EXCLUDED.color_code,
            updated_at = now();

        FOR s_idx IN 1..section_total LOOP
            section_id_v := seeded_uuid('section', c_idx * 10 + s_idx);

            INSERT INTO section (
                id, course_id, name, slug, order_index, estimated_duration_minutes, created_at, updated_at
            ) VALUES (
                section_id_v,
                course_id_v,
                format('Section %s.%s - Initial Response', c_idx, s_idx),
                format('section-%s-%s-initial-response', c_idx, s_idx),
                s_idx,
                30,
                now(),
                now()
            )
            ON CONFLICT (id) DO UPDATE SET
                course_id = EXCLUDED.course_id,
                name = EXCLUDED.name,
                slug = EXCLUDED.slug,
                order_index = EXCLUDED.order_index,
                estimated_duration_minutes = EXCLUDED.estimated_duration_minutes,
                updated_at = now();

            FOR l_idx IN 1..2 LOOP
                lesson_key := c_idx * 100 + s_idx * 10 + l_idx;
                lesson_id_v := seeded_uuid('lesson', lesson_key);
                assessment_id_v := seeded_uuid('assessment', lesson_key);

                q_mcq_id := seeded_uuid('question-mcq', lesson_key);
                q_fill_id := seeded_uuid('question-fill', lesson_key);
                q_short_id := seeded_uuid('question-short', lesson_key);
                q_match_id := seeded_uuid('question-match', lesson_key);
                q_order_id := seeded_uuid('question-order', lesson_key);
                q_hotspot_id := seeded_uuid('question-hotspot', lesson_key);

                INSERT INTO lesson (
                    id,
                    section_id,
                    name,
                    description,
                    slug,
                    order_index,
                    estimated_duration_minutes,
                    difficulty_level,
                    is_active,
                    content,
                    created_at,
                    updated_at
                ) VALUES (
                    lesson_id_v,
                    section_id_v,
                    format('Lesson %s.%s.%s - Airway Basics', c_idx, s_idx, l_idx),
                    'Mixed content and gradable blocks.',
                    format('lesson-%s-%s-%s-airway-basics', c_idx, s_idx, l_idx),
                    l_idx,
                    15,
                    'beginner',
                    true,
                    format('{"title":"Lesson %s.%s.%s","body":"ABC primary survey and first response workflow."}', c_idx, s_idx, l_idx),
                    now(),
                    now()
                )
                ON CONFLICT (id) DO UPDATE SET
                    section_id = EXCLUDED.section_id,
                    name = EXCLUDED.name,
                    description = EXCLUDED.description,
                    slug = EXCLUDED.slug,
                    order_index = EXCLUDED.order_index,
                    estimated_duration_minutes = EXCLUDED.estimated_duration_minutes,
                    difficulty_level = EXCLUDED.difficulty_level,
                    is_active = EXCLUDED.is_active,
                    content = EXCLUDED.content,
                    updated_at = now();

                -- non-gradable content blocks
                INSERT INTO lesson_content_block (
                    id, lesson_id, order_index, kind, payload, assessment_id, question_id, created_at, updated_at
                ) VALUES
                (
                    seeded_uuid('block-rich', lesson_key),
                    lesson_id_v,
                    1,
                    'RICH_TEXT',
                    '{"title":"Core Concept","content":"Scene safety first, then responsiveness, then airway."}',
                    null,
                    null,
                    now(),
                    now()
                ),
                (
                    seeded_uuid('block-info', lesson_key),
                    lesson_id_v,
                    2,
                    'INFOGRAPHIC',
                    '{"title":"ABC Overview","mediaType":"image","imageUrl":"https://example.com/abc-overview.png","caption":"A-B-C priorities"}',
                    null,
                    null,
                    now(),
                    now()
                ),
                (
                    seeded_uuid('block-timeline', lesson_key),
                    lesson_id_v,
                    3,
                    'TIMELINE',
                    '{"title":"Response Timeline","events":[{"title":"Assess scene","time":"0-10s"},{"title":"Check responsiveness","time":"10-20s"},{"title":"Open airway","time":"20-30s"}]}',
                    null,
                    null,
                    now(),
                    now()
                ),
                -- gradable blocks mapped to assessment questions
                (
                    seeded_uuid('block-mcq', lesson_key),
                    lesson_id_v,
                    4,
                    'QUIZ_MCQ',
                    '{"prompt":"What is the first action when finding an unconscious patient?","options":["Check responsiveness","Give water","Wait 10 minutes"]}',
                    assessment_id_v,
                    q_mcq_id,
                    now(),
                    now()
                ),
                (
                    seeded_uuid('block-fill', lesson_key),
                    lesson_id_v,
                    5,
                    'FILL_IN_THE_BLANKS',
                    '{"template":"A stands for ___ in ABC.","options":["airway","aspirin","activity"]}',
                    assessment_id_v,
                    q_fill_id,
                    now(),
                    now()
                ),
                (
                    seeded_uuid('block-short', lesson_key),
                    lesson_id_v,
                    6,
                    'SHORT_ANSWER',
                    '{"prompt":"Explain why airway assessment is important."}',
                    assessment_id_v,
                    q_short_id,
                    now(),
                    now()
                ),
                (
                    seeded_uuid('block-match', lesson_key),
                    lesson_id_v,
                    7,
                    'MATCHING',
                    '{"prompt":"Match terms and definitions.","pairs":[{"left":"Airway","right":"Breathing passage"},{"left":"Pulse","right":"Heart beats per minute"}]}',
                    assessment_id_v,
                    q_match_id,
                    now(),
                    now()
                ),
                (
                    seeded_uuid('block-order', lesson_key),
                    lesson_id_v,
                    8,
                    'ORDERING',
                    '{"prompt":"Order the steps.","items":[{"stableKey":"k1","text":"Ensure scene safety"},{"stableKey":"k2","text":"Check responsiveness"},{"stableKey":"k3","text":"Call for help"},{"stableKey":"k4","text":"Open airway"}]}',
                    assessment_id_v,
                    q_order_id,
                    now(),
                    now()
                ),
                (
                    seeded_uuid('block-hotspot', lesson_key),
                    lesson_id_v,
                    9,
                    'HOTSPOT_IMAGE',
                    '{"title":"Identify airway region","imageUrl":"https://upload.wikimedia.org/wikipedia/commons/thumb/8/8f/Upper_and_lower_respiratory_tract-en.svg/1200px-Upper_and_lower_respiratory_tract-en.svg.png","hotspots":[{"label":"Head"},{"label":"Neck/Airway"},{"label":"Arm"}]}',
                    assessment_id_v,
                    q_hotspot_id,
                    now(),
                    now()
                )
                ON CONFLICT (id) DO UPDATE SET
                    lesson_id = EXCLUDED.lesson_id,
                    order_index = EXCLUDED.order_index,
                    kind = EXCLUDED.kind,
                    payload = EXCLUDED.payload,
                    assessment_id = EXCLUDED.assessment_id,
                    question_id = EXCLUDED.question_id,
                    updated_at = now();

                INSERT INTO user_lesson (user_id, lesson_id, quizzes_correct, completed_at)
                VALUES (demo_user_id, lesson_id_v, 0, null)
                ON CONFLICT (user_id, lesson_id) DO UPDATE SET quizzes_correct = EXCLUDED.quizzes_correct;
            END LOOP;
        END LOOP;
    END LOOP;
END $$;

COMMIT;
